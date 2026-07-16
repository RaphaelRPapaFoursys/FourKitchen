package br.com.fourkitchen.ms_pedidos.service;

import br.com.fourkitchen.ms_pedidos.dto.request.*;
import br.com.fourkitchen.ms_pedidos.dto.response.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.PedidoCozinhaResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.PedidoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.ResumoContaAtendimentoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.ResumoPedidosOperacaoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.SinalizarProblemaResponse;
import br.com.fourkitchen.ms_pedidos.entities.Pedido;
import br.com.fourkitchen.ms_pedidos.entities.ProdutoPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusProdutoPedido;
import br.com.fourkitchen.ms_pedidos.exceptions.*;
import br.com.fourkitchen.ms_pedidos.mapper.CriarPedidoRequestMapper;
import br.com.fourkitchen.ms_pedidos.mapper.PedidoResponseMapper;
import br.com.fourkitchen.ms_pedidos.repository.PedidoRepository;
import br.com.fourkitchen.ms_pedidos.repository.ProdutoPedidoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class PedidoService {
    private static final Collection<StatusPedido> STATUS_ATIVOS = List.of(
            StatusPedido.ENVIADO_COZINHA,
            StatusPedido.EM_PREPARO,
            StatusPedido.PRONTO,
            StatusPedido.ENTREGUE,
            StatusPedido.PROBLEMA_COZINHA,
            StatusPedido.AGUARDANDO_DECISAO
    );

    // TODO: provisorio ate existir o passo de "pagar conta" (ENTREGUE -> FINALIZADO).
    // Enquanto o pagamento nao existe, pedidos ENTREGUE NAO bloqueiam o fechamento do
    // atendimento (validarMesaSemPedidosAtivos no ms-mesas); senao a mesa nunca fecharia.
    // Quando "pagar conta" for implementado, remover esta constante e voltar o
    // possuiPedidosAtivos a usar STATUS_ATIVOS (ENTREGUE deve voltar a bloquear; so
    // FINALIZADO/CANCELADO liberam o fechamento).
    private static final Collection<StatusPedido> STATUS_BLOQUEIA_FECHAMENTO = List.of(
            StatusPedido.ENVIADO_COZINHA,
            StatusPedido.EM_PREPARO,
            StatusPedido.PRONTO,
            StatusPedido.AGUARDANDO_DECISAO
    );

    private static final Collection<StatusPedido> STATUS_COZINHA = List.of(
            StatusPedido.ENVIADO_COZINHA,
            StatusPedido.EM_PREPARO,
            StatusPedido.AGUARDANDO_DECISAO
    );

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoPedidoRepository produtoPedidoRepository;

    @Autowired
    private PedidoResponseMapper pedidoResponseMapper;

    @Autowired
    private CriarPedidoRequestMapper criarPedidoRequestMapper;

    @Transactional
    public PedidoResponse createPedido(CriarPedidoRequest pedidoRequest) {
        Pedido pedido = criarPedidoRequestMapper.map(pedidoRequest);

        if (pedido.getCodigo() == null) {
            pedido.setCodigo(gerarCodigoPedidoUnico());
        }

        pedido.setStatus(StatusPedido.ENVIADO_COZINHA);

        pedidoRepository.saveAndFlush(pedido);

        List<ProdutoPedido> produtosPedido = new ArrayList<>();

        if (pedidoRequest.itens() != null) {
            for (ProdutoPedidoRequest item : pedidoRequest.itens()) {
                ProdutoPedido produtoPedido = ProdutoPedido
                        .builder()
                        .quantidade(item.quantidade())
                        .idPedido(pedido.getId())
                        .idProduto(item.idProduto())
                        .nomeProduto(item.nomeProduto())
                        .precoUnitario(item.precoUnitario())
                        .observacao(item.observacao())
                        .status(StatusProdutoPedido.DISPONIVEL)
                        .build();

                produtosPedido.add(produtoPedido);
            }
        }

        produtoPedidoRepository.saveAll(produtosPedido);

        return pedidoResponseMapper.map(pedido);
    }

    public PedidoResponse findById(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorEnum.PEDIDO_NAO_ENCONTRADO));

        return pedidoResponseMapper.map(pedido);
    }

    public List<PedidoResponse> findAll() {
        List<PedidoResponse> listaPedidos = pedidoRepository.findAll()
                .stream()
                .map(pedidoResponseMapper::map)
                .toList();

        return listaPedidos;
    }

    public List<PedidoResponse> findPedidosCozinha() {
        return pedidoRepository.findByStatusIn(STATUS_COZINHA)
                .stream()
                .map(pedidoResponseMapper::map)
                .toList();
    }

    public List<PedidoCozinhaResponse> findFilaCozinha() {
        List<Pedido> pedidos = pedidoRepository.findByStatusInOrderByDataCriacaoAscIdAsc(STATUS_COZINHA);

        if (pedidos.isEmpty()) {
            return List.of();
        }

        List<Integer> idsPedidos = pedidos.stream()
                .map(Pedido::getId)
                .toList();

        Map<Integer, List<ProdutoPedido>> itensPorPedido = produtoPedidoRepository.findByIdPedidoIn(idsPedidos)
                .stream()
                .filter(this::itemAtivo)
                .collect(Collectors.groupingBy(ProdutoPedido::getIdPedido));

        return pedidos.stream()
                .map(pedido -> mapearPedidoCozinha(pedido, itensPorPedido.getOrDefault(pedido.getId(), List.of())))
                .toList();
    }

    public boolean possuiPedidosAtivos(Integer atendimentoId) {
        return pedidoRepository.existsByIdAtendimentoAndStatusIn(atendimentoId, STATUS_BLOQUEIA_FECHAMENTO);
    }

    public List<PedidoResponse> findPedidosAtivosPorAtendimentos(List<Integer> idsAtendimento) {
        if (idsAtendimento == null || idsAtendimento.isEmpty()) {
            return List.of();
        }

        return pedidoRepository
                .findByIdAtendimentoInAndStatusInOrderByDataCriacaoAscIdAsc(idsAtendimento, STATUS_ATIVOS)
                .stream()
                .map(pedidoResponseMapper::map)
                .toList();
    }

    public ResumoPedidosOperacaoResponse buscarResumoOperacao() {
        return new ResumoPedidosOperacaoResponse(
                pedidoRepository.countByStatus(StatusPedido.EM_PREPARO),
                pedidoRepository.countByStatus(StatusPedido.PRONTO),
                pedidoRepository.countByStatus(StatusPedido.PROBLEMA_COZINHA)
                        + pedidoRepository.countByStatus(StatusPedido.AGUARDANDO_DECISAO)
        );
    }

    public ResumoContaAtendimentoResponse buscarResumoContaAtendimento(Integer idAtendimento) {
        List<Pedido> pedidos = pedidoRepository
                .findByIdAtendimentoAndStatusNotOrderByDataCriacaoAscIdAsc(
                        idAtendimento,
                        StatusPedido.CANCELADO
                );

        if (pedidos.isEmpty()) {
            return new ResumoContaAtendimentoResponse(idAtendimento, BigDecimal.ZERO, 0, 0);
        }

        List<Integer> idsPedidos = pedidos.stream()
                .map(Pedido::getId)
                .toList();

        List<ProdutoPedido> itens = produtoPedidoRepository.findByIdPedidoIn(idsPedidos)
                .stream()
                .filter(this::itemAtivo)
                .toList();

        BigDecimal valorFinal = itens.stream()
                .map(this::valorItem)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItens = itens.stream()
                .mapToInt(item -> item.getQuantidade() == null ? 0 : item.getQuantidade())
                .sum();

        return new ResumoContaAtendimentoResponse(
                idAtendimento,
                valorFinal,
                pedidos.size(),
                totalItens
        );
    }

    public List<PedidoCozinhaResponse> findPedidosAtivosDetalhadosPorAtendimentos(List<Integer> idsAtendimento) {
        if (idsAtendimento == null || idsAtendimento.isEmpty()) {
            return List.of();
        }

        List<Pedido> pedidos = pedidoRepository
                .findByIdAtendimentoInAndStatusInOrderByDataCriacaoAscIdAsc(idsAtendimento, STATUS_ATIVOS);

        if (pedidos.isEmpty()) {
            return List.of();
        }

        List<Integer> idsPedidos = pedidos.stream()
                .map(Pedido::getId)
                .toList();

        Map<Integer, List<ProdutoPedido>> itensPorPedido = produtoPedidoRepository.findByIdPedidoIn(idsPedidos)
                .stream()
                .filter(this::itemAtivo)
                .collect(Collectors.groupingBy(ProdutoPedido::getIdPedido));

        return pedidos.stream()
                .map(pedido -> mapearPedidoCozinha(pedido, itensPorPedido.getOrDefault(pedido.getId(), List.of())))
                .toList();
    }

    public List<PedidoCozinhaResponse> findPedidosDetalhadosPorAtendimento(Integer idAtendimento) {
        if (idAtendimento == null || idAtendimento <= 0) {
            return List.of();
        }

        List<Pedido> pedidos = pedidoRepository.findByIdAtendimentoOrderByDataCriacaoAscIdAsc(idAtendimento);

        if (pedidos.isEmpty()) {
            return List.of();
        }

        List<Integer> idsPedidos = pedidos.stream()
                .map(Pedido::getId)
                .toList();

        Map<Integer, List<ProdutoPedido>> itensPorPedido = produtoPedidoRepository.findByIdPedidoIn(idsPedidos)
                .stream()
                .collect(Collectors.groupingBy(ProdutoPedido::getIdPedido));

        return pedidos.stream()
                .map(pedido -> mapearPedidoCozinha(pedido, itensPorPedido.getOrDefault(pedido.getId(), List.of())))
                .toList();
    }

    @Transactional
    public PedidoResponse iniciarPreparo(Integer id) {
        Pedido pedido = buscarPedidoParaAtualizacao(id);

        validarPedidoNaoAguardandoDecisao(pedido);

        validarStatusAtual(pedido, StatusPedido.ENVIADO_COZINHA);
        pedido.setStatus(StatusPedido.EM_PREPARO);
        pedido.setDataInicioPreparo(LocalDateTime.now());

        return pedidoResponseMapper.map(pedido);
    }

    @Transactional
    public PedidoResponse finalizarPreparo(Integer id) {
        Pedido pedido = buscarPedido(id);

        validarPedidoNaoAguardandoDecisao(pedido);

        validarStatusAtual(pedido, StatusPedido.EM_PREPARO);
        if (pedido.getDataInicioPreparo() == null) {
            pedido.setDataInicioPreparo(LocalDateTime.now());
        }
        pedido.setStatus(StatusPedido.PRONTO);
        pedido.setDataPronto(LocalDateTime.now());

        return pedidoResponseMapper.map(pedido);
    }

    @Transactional
    public PedidoResponse entregarPedido(Integer id) {
        Pedido pedido = buscarPedido(id);

        validarStatusAtual(pedido, StatusPedido.PRONTO);
        pedido.setStatus(StatusPedido.ENTREGUE);

        return pedidoResponseMapper.map(pedido);
    }

    @Transactional
    public void patchPedido(Integer id, AlterarPedidoRequest alterarPedidoRequest) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorEnum.PEDIDO_NAO_ENCONTRADO));

        if (alterarPedidoRequest.canal() != null) {
            pedido.setCanal(alterarPedidoRequest.canal());
        }

        validarPedidoNaoAguardandoDecisao(pedido);

        if (alterarPedidoRequest.status() != null) {
            pedido.setStatus(alterarPedidoRequest.status());
        }

        if (alterarPedidoRequest.idMesa() != null) {
            pedido.setIdMesa(alterarPedidoRequest.idMesa());
        }

        if (alterarPedidoRequest.idUsuario() != null) {
            pedido.setIdUsuario(alterarPedidoRequest.idUsuario());
        }

        if (alterarPedidoRequest.idAtendimento() != null) {
            pedido.setIdAtendimento(alterarPedidoRequest.idAtendimento());
        }
    }

    @Transactional
    public void softDelete(Integer id) {
        cancelarPedidoAntesDoPreparo(id);
    }

    @Transactional
    public void cancelarPedidoAntesDoPreparo(Integer id) {
        Pedido pedido = buscarPedidoParaAtualizacao(id);

        validarPedidoPodeSerCanceladoAntesDoPreparo(pedido);
        pedido.setStatus(StatusPedido.CANCELADO);
    }

    private Integer gerarCodigoPedidoUnico() {
        Integer codigo;

        do {
            codigo = ThreadLocalRandom.current().nextInt(100000, 1000000);
        } while (pedidoRepository.existsByCodigo(codigo));

        return codigo;
    }

    private Pedido buscarPedido(Integer id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorEnum.PEDIDO_NAO_ENCONTRADO));
    }

    private void validarStatusAtual(Pedido pedido, StatusPedido statusEsperado) {
        if (!statusEsperado.equals(pedido.getStatus())) {
            throw new BaseException(ErrorEnum.TRANSICAO_STATUS_INVALIDA);
        }
    }

    private PedidoCozinhaResponse mapearPedidoCozinha(Pedido pedido, List<ProdutoPedido> itens) {
        return new PedidoCozinhaResponse(
                pedido.getId(),
                pedido.getCodigo(),
                pedido.getCanal(),
                pedido.getStatus(),
                pedido.getIdMesa(),
                pedido.getIdUsuario(),
                pedido.getIdAtendimento(),
                pedido.getDataCriacao(),
                pedido.getDataInicioPreparo(),
                pedido.getDataPronto(),
                itens.stream()
                        .map(this::mapearItemCozinha)
                        .toList()
        );
    }

    private ItemPedidoCozinhaResponse mapearItemCozinha(ProdutoPedido item) {
        return new ItemPedidoCozinhaResponse(
                item.getId(),
                item.getIdProduto(),
                item.getNomeProduto(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getObservacao(),
                item.getStatus()
        );
    }

    private BigDecimal valorItem(ProdutoPedido item) {
        if (item.getPrecoUnitario() == null || item.getQuantidade() == null) {
            return BigDecimal.ZERO;
        }

        return item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
    }

    private boolean itemAtivo(ProdutoPedido item) {
        return item.getStatus() != StatusProdutoPedido.REMOVIDO
                && item.getStatus() != StatusProdutoPedido.CANCELADO;
    }

    private void validarPedidoNaoAguardandoDecisao(Pedido pedido) {
        if (pedidoPermiteDecisao(pedido)) {
            throw new BaseException(ErrorEnum.PEDIDO_AGUARDANDO_DECISAO);
        }
    }

    @Transactional
    public SinalizarProblemaResponse sinalizarProblema(SinalizarProblemaRequest request) {

        Pedido pedido = pedidoRepository.findByIdForUpdate(request.idPedido())
                .orElseThrow(() -> new BaseException(ErrorEnum.PEDIDO_NAO_ENCONTRADO));

        StatusPedido status = pedido.getStatus();

        if (status != StatusPedido.ENVIADO_COZINHA) {
            throw new BaseException(ErrorEnum.PEDIDO_ENCERRADO);
        }

        ProdutoPedido produtoPedido = produtoPedidoRepository
                .findByIdPedidoAndId(
                        request.idPedido(),
                        request.idProdutoPedido()
                ).orElseThrow(() -> new BaseException(ErrorEnum.PRODUTO_PEDIDO_NAO_ENCONTRADO));

        pedido.setStatus(StatusPedido.AGUARDANDO_DECISAO);
        produtoPedido.setStatus(request.statusProdutoPedido());

        return new SinalizarProblemaResponse(
                pedido.getId(),
                produtoPedido.getId(),
                pedido.getStatus(),
                produtoPedido.getStatus()
        );
    }

    @Transactional
    public void decisaoProblema(DecisaoProblemaRequest decisaoProblemaRequest) {
        Pedido pedido = pedidoRepository.findById(decisaoProblemaRequest.idPedido())
                .orElseThrow(() -> new BaseException(ErrorEnum.PEDIDO_NAO_ENCONTRADO));

        ProdutoPedido produtoPedido = produtoPedidoRepository.findById(decisaoProblemaRequest.idProdutoPedido())
                .orElseThrow(() -> new BaseException(ErrorEnum.PRODUTO_PEDIDO_NAO_ENCONTRADO));

        if (!pedidoPermiteDecisao(pedido)) {
            throw new BaseException(ErrorEnum.PEDIDO_NAO_PERMITE_DECISAO);
        }

        if (decisaoProblemaRequest.pedidoCancelado()) {
            validarPedidoPodeSerCanceladoAntesDoPreparo(pedido);
            pedido.setStatus(StatusPedido.CANCELADO);
            pedidoRepository.save(pedido);
            return;
        }

        if (decisaoProblemaRequest.novoStatusProdutoPedido().equals(StatusProdutoPedido.REMOVIDO)) {
            produtoPedido.setStatus(StatusProdutoPedido.REMOVIDO);

            boolean possuiItensAtivos = produtoPedidoRepository
                    .findByIdPedidoIn(List.of(decisaoProblemaRequest.idPedido()))
                    .stream()
                    .anyMatch(this::itemAtivo);

            if (!possuiItensAtivos) {
                validarPedidoPodeSerCanceladoAntesDoPreparo(pedido);
                pedido.setStatus(StatusPedido.CANCELADO);
                pedidoRepository.save(pedido);
                produtoPedidoRepository.save(produtoPedido);
                return;
            }
        }

        if (decisaoProblemaRequest.idNovoProduto() != null) {
            produtoPedido.setIdProduto(decisaoProblemaRequest.idNovoProduto());
            produtoPedido.setNomeProduto(decisaoProblemaRequest.nomeNovoProduto());
            if (decisaoProblemaRequest.precoNovoProduto() != null) {
                produtoPedido.setPrecoUnitario(decisaoProblemaRequest.precoNovoProduto());
            }
            // A observacao pertence ao produto substituto; a anterior nunca e reaproveitada.
            produtoPedido.setObservacao(normalizarObservacao(decisaoProblemaRequest.observacaoNovoProduto()));
        }

        produtoPedido.setStatus(decisaoProblemaRequest.novoStatusProdutoPedido());
        pedido.setStatus(StatusPedido.ENVIADO_COZINHA);

        pedidoRepository.save(pedido);
        produtoPedidoRepository.save(produtoPedido);
    }

    private boolean pedidoPermiteDecisao(Pedido pedido) {
        return pedido.getStatus() == StatusPedido.AGUARDANDO_DECISAO
                || pedido.getStatus() == StatusPedido.PROBLEMA_COZINHA;
    }

    private Pedido buscarPedidoParaAtualizacao(Integer id) {
        return pedidoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BaseException(ErrorEnum.PEDIDO_NAO_ENCONTRADO));
    }

    private void validarPedidoPodeSerCanceladoAntesDoPreparo(Pedido pedido) {
        boolean pedidoEnviadoOuAguardandoDecisao = pedido.getStatus() == StatusPedido.ENVIADO_COZINHA
                || pedidoPermiteDecisao(pedido);

        if (!pedidoEnviadoOuAguardandoDecisao || pedido.getDataInicioPreparo() != null) {
            throw new BaseException(ErrorEnum.TRANSICAO_STATUS_INVALIDA);
        }
    }

    private String normalizarObservacao(String observacao) {
        if (observacao == null || observacao.isBlank()) {
            return null;
        }

        return observacao.trim();
    }
}
