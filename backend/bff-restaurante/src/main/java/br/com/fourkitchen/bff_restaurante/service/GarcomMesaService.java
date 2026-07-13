package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaClientResponse;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaGarcomClientResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ResumoContaAtendimentoResponse;
import br.com.fourkitchen.bff_restaurante.client.notificacoes.NotificacaoClient;
import br.com.fourkitchen.bff_restaurante.dto.response.ContaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.FechamentoContaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ItemPedidoDetalheGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaDetalheGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGarcomDetalheResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaProblemasGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoDetalheGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ProblemaPedidoGarcomResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.mapper.MesaGarcomMapperSource;
import br.com.fourkitchen.bff_restaurante.mapper.MesaGarcomResponseMapper;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GarcomMesaService {
    private static final List<String> STATUS_BLOQUEIA_FECHAMENTO = List.of(
            "ENVIADO_COZINHA",
            "EM_PREPARO",
            "PRONTO",
            "AGUARDANDO_DECISAO",
            "PROBLEMA_COZINHA"
    );

    private static final List<String> STATUS_PROBLEMA_ITEM = List.of(
            "FALTA_PRODUTO",
            "ERRO",
            "INDISPONIVEL"
    );

    private static final List<String> STATUS_AGUARDA_DECISAO = List.of(
            "AGUARDANDO_DECISAO",
            "PROBLEMA_COZINHA"
    );

    private final MesaClient mesaClient;

    private final PedidoClient pedidoClient;

    private final NotificacaoClient notificacaoClient;

    private final MesaGarcomResponseMapper mesaGarcomResponseMapper;

    public List<MesaGarcomResponse> listarMesas(Authentication authentication) {
        Integer idGarcom = extrairIdGarcom(authentication);
        List<MesaGarcomClientResponse> mesas = buscarMesasDoGarcom(idGarcom);

        if (mesas.isEmpty()) {
            return List.of();
        }

        List<Integer> idsAtendimento = mesas.stream()
                .map(MesaGarcomClientResponse::idAtendimento)
                .filter(idAtendimento -> idAtendimento != null)
                .toList();

        Map<Integer, List<PedidoResponse>> pedidosPorAtendimento = buscarPedidosAtivos(idsAtendimento)
                .stream()
                .collect(Collectors.groupingBy(PedidoResponse::idAtendimento));

        Map<Integer, List<NotificacaoResponse>> chamadasPorAtendimento = buscarChamadasPendentes(idsAtendimento)
                .stream()
                .collect(Collectors.groupingBy(NotificacaoResponse::idAtendimento));

        return mesas.stream()
                .map(mesa -> mesaGarcomResponseMapper.map(new MesaGarcomMapperSource(
                        mesa,
                        pedidosPorAtendimento.getOrDefault(mesa.idAtendimento(), List.of()),
                        chamadasPorAtendimento.getOrDefault(mesa.idAtendimento(), List.of())
                )))
                .toList();
    }

    public MesaGarcomDetalheResponse detalharMesa(Integer idMesa, Authentication authentication) {
        Integer idGarcom = extrairIdGarcom(authentication);
        MesaGarcomClientResponse mesa = buscarMesaAtribuidaAoGarcom(idMesa, idGarcom);
        validarAtendimentoAberto(mesa);

        List<PedidoCozinhaResponse> pedidos = buscarPedidosDetalhados(mesa.idAtendimento());
        ContaGarcomResponse conta = mapearConta(buscarResumoConta(mesa.idAtendimento()));

        return new MesaGarcomDetalheResponse(
                mapearMesaDetalhe(mesa),
                conta,
                pedidos.stream().map(this::mapearPedidoDetalhe).toList(),
                mapearProblemas(pedidos)
        );
    }

    public MesaProblemasGarcomResponse listarProblemasDaMesa(
            Integer idMesa,
            Authentication authentication
    ) {
        Integer idGarcom = extrairIdGarcom(authentication);
        MesaGarcomClientResponse mesa = buscarMesaAtribuidaAoGarcom(idMesa, idGarcom);
        validarAtendimentoAberto(mesa);

        List<PedidoCozinhaResponse> pedidos = buscarPedidosDetalhados(mesa.idAtendimento())
                .stream()
                .filter(pedido -> STATUS_AGUARDA_DECISAO.contains(pedido.status()))
                .toList();

        return new MesaProblemasGarcomResponse(
                mapearMesaDetalhe(mesa),
                pedidos.stream().map(this::mapearPedidoDetalhe).toList(),
                mapearProblemas(pedidos)
        );
    }

    public List<PedidoDetalheGarcomResponse> listarPedidosDaMesa(Integer idMesa, Authentication authentication) {
        Integer idGarcom = extrairIdGarcom(authentication);
        MesaGarcomClientResponse mesa = buscarMesaAtribuidaAoGarcom(idMesa, idGarcom);
        validarAtendimentoAberto(mesa);

        return buscarPedidosDetalhados(mesa.idAtendimento())
                .stream()
                .map(this::mapearPedidoDetalhe)
                .toList();
    }

    public FechamentoContaGarcomResponse fecharConta(Integer idMesa, Authentication authentication) {
        Integer idGarcom = extrairIdGarcom(authentication);
        MesaGarcomClientResponse mesa = buscarMesaAtribuidaAoGarcom(idMesa, idGarcom);
        validarAtendimentoAberto(mesa);

        List<PedidoCozinhaResponse> pedidos = buscarPedidosDetalhados(mesa.idAtendimento());
        if (pedidos.stream().anyMatch(this::pedidoBloqueiaFechamento)) {
            throw new BaseException(ErrorEnum.CONTA_NAO_PODE_FECHAR);
        }

        ContaGarcomResponse conta = mapearConta(buscarResumoConta(mesa.idAtendimento()));
        MesaClientResponse mesaFechada = fecharMesaNoMsMesas(idMesa);

        return new FechamentoContaGarcomResponse(
                mesa.idMesa(),
                mesa.numero(),
                mesaFechada.status(),
                mesa.idAtendimento(),
                mesa.codigoSessao(),
                mesa.dataAbertura(),
                mesaFechada.dataFechamento(),
                conta
        );
    }

    private Integer extrairIdGarcom(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuarioAutenticado)) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        if (usuarioAutenticado.id() == null) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        try {
            return Math.toIntExact(usuarioAutenticado.id());
        } catch (ArithmeticException e) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
    }

    private List<MesaGarcomClientResponse> buscarMesasDoGarcom(Integer idGarcom) {
        try {
            return mesaClient.listarMesasPorGarcom(idGarcom);
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private MesaGarcomClientResponse buscarMesaAtribuidaAoGarcom(Integer idMesa, Integer idGarcom) {
        if (idMesa == null || idMesa <= 0) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }

        return buscarMesasDoGarcom(idGarcom)
                .stream()
                .filter(mesa -> idMesa.equals(mesa.idMesa()))
                .findFirst()
                .orElseThrow(() -> new BaseException(ErrorEnum.MESA_NAO_ATRIBUIDA_AO_GARCOM));
    }

    private void validarAtendimentoAberto(MesaGarcomClientResponse mesa) {
        if (mesa.idAtendimento() == null) {
            throw new BaseException(ErrorEnum.ATENDIMENTO_NAO_ABERTO);
        }
    }

    private List<PedidoResponse> buscarPedidosAtivos(List<Integer> idsAtendimento) {
        if (idsAtendimento.isEmpty()) {
            return List.of();
        }

        try {
            return pedidoClient.listarPedidosAtivosPorAtendimentos(idsAtendimento);
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private List<NotificacaoResponse> buscarChamadasPendentes(List<Integer> idsAtendimento) {
        if (idsAtendimento.isEmpty()) {
            return List.of();
        }

        try {
            return notificacaoClient.listarChamadasPendentesPorAtendimentos(idsAtendimento);
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_NOTIFICACOES_INDISPONIVEL);
        }
    }

    private List<PedidoCozinhaResponse> buscarPedidosDetalhados(Integer idAtendimento) {
        try {
            return pedidoClient.listarPedidosDetalhadosPorAtendimento(idAtendimento);
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private ResumoContaAtendimentoResponse buscarResumoConta(Integer idAtendimento) {
        try {
            return pedidoClient.buscarResumoContaAtendimento(idAtendimento);
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private MesaClientResponse fecharMesaNoMsMesas(Integer idMesa) {
        try {
            return mesaClient.fecharMesa(idMesa);
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.CONTA_NAO_PODE_FECHAR);
            }

            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private MesaDetalheGarcomResponse mapearMesaDetalhe(MesaGarcomClientResponse mesa) {
        return new MesaDetalheGarcomResponse(
                mesa.idMesa(),
                mesa.numero(),
                mesa.status(),
                mesa.idAtendimento(),
                mesa.codigoSessao(),
                mesa.dataAbertura()
        );
    }

    private ContaGarcomResponse mapearConta(ResumoContaAtendimentoResponse resumo) {
        BigDecimal total = resumo.valorFinal() == null ? BigDecimal.ZERO : resumo.valorFinal();

        return new ContaGarcomResponse(
                total,
                total,
                resumo.totalPedidos(),
                resumo.totalItens()
        );
    }

    private PedidoDetalheGarcomResponse mapearPedidoDetalhe(PedidoCozinhaResponse pedido) {
        return new PedidoDetalheGarcomResponse(
                pedido.id(),
                pedido.codigo(),
                pedido.canal(),
                pedido.status(),
                pedido.dataCriacao(),
                pedido.dataInicioPreparo(),
                pedido.dataPronto(),
                listarItens(pedido).stream().map(this::mapearItemDetalhe).toList()
        );
    }

    private ItemPedidoDetalheGarcomResponse mapearItemDetalhe(ItemPedidoCozinhaResponse item) {
        return new ItemPedidoDetalheGarcomResponse(
                item.id(),
                item.idProduto(),
                item.nomeProduto(),
                item.quantidade(),
                item.precoUnitario(),
                item.observacao(),
                item.status()
        );
    }

    private List<ProblemaPedidoGarcomResponse> mapearProblemas(List<PedidoCozinhaResponse> pedidos) {
        return pedidos.stream()
                .flatMap(pedido -> listarItens(pedido).stream()
                        .filter(item -> STATUS_PROBLEMA_ITEM.contains(item.status()))
                        .map(item -> new ProblemaPedidoGarcomResponse(
                                pedido.id(),
                                item.id(),
                                item.status(),
                                mensagemProblema(item.status())
                        )))
                .toList();
    }

    private List<ItemPedidoCozinhaResponse> listarItens(PedidoCozinhaResponse pedido) {
        return pedido.itens() == null ? List.of() : pedido.itens();
    }

    private boolean pedidoBloqueiaFechamento(PedidoCozinhaResponse pedido) {
        return STATUS_BLOQUEIA_FECHAMENTO.contains(pedido.status());
    }

    private String mensagemProblema(String status) {
        return switch (status) {
            case "FALTA_PRODUTO" -> "Item com falta de produto";
            case "ERRO" -> "Item sinalizado com erro";
            case "INDISPONIVEL" -> "Produto indisponivel";
            default -> "Item com problema";
        };
    }
}
