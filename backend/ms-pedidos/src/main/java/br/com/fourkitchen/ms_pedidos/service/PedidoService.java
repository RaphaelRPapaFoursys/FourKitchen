package br.com.fourkitchen.ms_pedidos.service;

import br.com.fourkitchen.ms_pedidos.dto.request.AlterarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.CriarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.ProdutoPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.response.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.PedidoCozinhaResponse;
import br.com.fourkitchen.ms_pedidos.dto.request.SinalizarProblemaRequest;
import br.com.fourkitchen.ms_pedidos.dto.response.PedidoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.ResumoContaAtendimentoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.ResumoPedidosOperacaoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.SinalizarProblemaResponse;
import br.com.fourkitchen.ms_pedidos.entities.Pedido;
import br.com.fourkitchen.ms_pedidos.entities.ProdutoPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import br.com.fourkitchen.ms_pedidos.exceptions.*;
import br.com.fourkitchen.ms_pedidos.mapper.CriarPedidoRequestMapper;
import br.com.fourkitchen.ms_pedidos.mapper.PedidoResponseMapper;
import br.com.fourkitchen.ms_pedidos.repository.PedidoRepository;
import br.com.fourkitchen.ms_pedidos.repository.ProdutoPedidoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
            StatusPedido.AGUARDANDO_DECISAO
    );

    private static final Collection<StatusPedido> STATUS_COZINHA = List.of(
            StatusPedido.ENVIADO_COZINHA,
            StatusPedido.EM_PREPARO
    );

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoPedidoRepository produtoPedidoRepository;

    @Autowired
    private PedidoResponseMapper pedidoResponseMapper;

    @Autowired
    private CriarPedidoRequestMapper criarPedidoRequestMapper;

    public PedidoResponse createPedido(CriarPedidoRequest pedidoRequest) {
        Pedido pedido = criarPedidoRequestMapper.map(pedidoRequest);

        if (pedido.getCodigo() == null) {
            pedido.setCodigo(gerarCodigoPedidoUnico());
        }

        pedido.setStatus(StatusPedido.ENVIADO_COZINHA);

        pedidoRepository.save(pedido);

        if (pedidoRequest.itens() != null) {
            for (ProdutoPedidoRequest item : pedidoRequest.itens()) {
                ProdutoPedido produtoPedido = ProdutoPedido
                        .builder()
                        .quantidade(item.quantidade())
                        .idPedido(pedido.getId())
                        .idProduto(item.idProduto())
                        .precoUnitario(item.precoUnitario())
                        .observacao(item.observacao())
                        .build();

                produtoPedidoRepository.save(produtoPedido);
            }
        }

        return pedidoResponseMapper.map(pedido);
    }

    public PedidoResponse findById(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)

                .orElseThrow(PedidoInexistenteException::new);

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
                .collect(Collectors.groupingBy(ProdutoPedido::getIdPedido));

        return pedidos.stream()
                .map(pedido -> mapearPedidoCozinha(pedido, itensPorPedido.getOrDefault(pedido.getId(), List.of())))
                .toList();
    }

    public boolean possuiPedidosAtivos(Integer atendimentoId) {
        return pedidoRepository.existsByIdAtendimentoAndStatusIn(atendimentoId, STATUS_ATIVOS);
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
                pedidoRepository.countByStatus(StatusPedido.AGUARDANDO_DECISAO)
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

        List<ProdutoPedido> itens = produtoPedidoRepository.findByIdPedidoIn(idsPedidos);

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
                .collect(Collectors.groupingBy(ProdutoPedido::getIdPedido));

        return pedidos.stream()
                .map(pedido -> mapearPedidoCozinha(pedido, itensPorPedido.getOrDefault(pedido.getId(), List.of())))
                .toList();
    }

    @Transactional
    public PedidoResponse iniciarPreparo(Integer id) {
        Pedido pedido = buscarPedido(id);

        validarPedidoNaoAguardandoDecisao(pedido);

        validarStatusAtual(pedido, StatusPedido.ENVIADO_COZINHA);
        pedido.setStatus(StatusPedido.EM_PREPARO);

        return pedidoResponseMapper.map(pedido);
    }

    @Transactional
    public PedidoResponse finalizarPreparo(Integer id) {
        Pedido pedido = buscarPedido(id);

        validarPedidoNaoAguardandoDecisao(pedido);

        validarStatusAtual(pedido, StatusPedido.EM_PREPARO);
        pedido.setStatus(StatusPedido.PRONTO);

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
                .orElseThrow(PedidoInexistenteException::new);

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
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(PedidoInexistenteException::new);

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
                .orElseThrow(PedidoInexistenteException::new);
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
                pedido.getIdAtendimento(),
                pedido.getDataCriacao(),
                itens.stream()
                        .map(this::mapearItemCozinha)
                        .toList()
        );
    }

    private ItemPedidoCozinhaResponse mapearItemCozinha(ProdutoPedido item) {
        return new ItemPedidoCozinhaResponse(
                item.getId(),
                item.getIdProduto(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getObservacao()
        );
    }

    private BigDecimal valorItem(ProdutoPedido item) {
        if (item.getPrecoUnitario() == null || item.getQuantidade() == null) {
            return BigDecimal.ZERO;
        }

        return item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
    }

    private void validarPedidoNaoAguardandoDecisao(Pedido pedido) {
        if (pedido.getStatus() == StatusPedido.AGUARDANDO_DECISAO) {
            throw new PedidoAguardandoDecisaoException();
        }
    }

    @Transactional
    public SinalizarProblemaResponse sinalizarProblema(SinalizarProblemaRequest request) {

        Pedido pedido = pedidoRepository.findById(request.idPedido()).orElseThrow(PedidoInexistenteException::new);

        ProdutoPedido produtoPedido = produtoPedidoRepository
                .findByIdPedidoAndId(
                        request.idPedido(),
                        request.idProdutoPedido()
                ).orElseThrow(ProdutoPedidoInexistenteException::new);

        StatusPedido status = pedido.getStatus();

        if (status != StatusPedido.ENVIADO_COZINHA
                && status != StatusPedido.EM_PREPARO) {
            throw new PedidoEncerradoException();
        }

        pedido.setStatus(StatusPedido.AGUARDANDO_DECISAO);
        produtoPedido.setStatus(request.statusProdutoPedido());

        return new SinalizarProblemaResponse(
                pedido.getId(),
                produtoPedido.getId(),
                pedido.getStatus(),
                produtoPedido.getStatus()
        );
    }

}
