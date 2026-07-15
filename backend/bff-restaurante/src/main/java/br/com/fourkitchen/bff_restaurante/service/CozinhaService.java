package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaClientResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.*;
import br.com.fourkitchen.bff_restaurante.client.usuarios.UsuarioClient;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.EventoPedido;
import br.com.fourkitchen.bff_restaurante.dto.TipoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.DecisaoProblemaRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.ItemFilaCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoFilaCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoStatusCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.enums.StatusProdutoPedido;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CozinhaService {

    private final PedidoClient pedidoClient;

    private final MesaClient mesaClient;

    private final UsuarioClient usuarioClient;

    private final NotificacaoService notificacaoService;

    private final DecisaoProblemaService decisaoProblemaService;

    public List<PedidoFilaCozinhaResponse> listarFila(String authorization) {
        List<PedidoCozinhaResponse> pedidos = listarPedidosDaFila();
        Map<Integer, Integer> numerosMesas = listarNumerosMesas();
        Map<Integer, UsuarioClientResponse> usuarios = listarUsuariosDosTotens(pedidos, authorization);

        return pedidos.stream()
                .map(pedido -> mapearPedido(pedido, numerosMesas, usuarios))
                .toList();
    }

    private List<PedidoCozinhaResponse> listarPedidosDaFila() {
        try {
            return pedidoClient.listarFilaCozinha();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private Map<Integer, Integer> listarNumerosMesas() {
        try {
            return mesaClient.listarMesas().stream()
                    .filter(mesa -> mesa.id() != null && mesa.numero() != null)
                    .collect(Collectors.toMap(MesaClientResponse::id, MesaClientResponse::numero));
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private Map<Integer, UsuarioClientResponse> listarUsuariosDosTotens(
            List<PedidoCozinhaResponse> pedidos,
            String authorization
    ) {
        boolean existemPedidosDeTotem = pedidos.stream()
                .anyMatch(pedido -> "TOTEM".equalsIgnoreCase(pedido.canal()) && pedido.idUsuario() != null);

        if (!existemPedidosDeTotem) {
            return Map.of();
        }

        try {
            return usuarioClient.listarUsuariosAtivos(authorization).stream()
                    .collect(Collectors.toMap(UsuarioClientResponse::id, Function.identity()));
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_USUARIOS_INDISPONIVEL);
        }
    }

    public PedidoStatusCozinhaResponse iniciarPreparo(Integer id) {
        PedidoResponse pedido = alterarStatus(id, EventoPedido.PEDIDO_EM_PREPARO);
        registrarEvento(EventoPedido.PEDIDO_EM_PREPARO);

        return mapearStatus(pedido);
    }

    public PedidoStatusCozinhaResponse finalizarPreparo(Integer id) {
        PedidoResponse pedido = alterarStatus(id, EventoPedido.PEDIDO_PRONTO);
        registrarEvento(EventoPedido.PEDIDO_PRONTO);

        return mapearStatus(pedido);
    }

    private PedidoResponse alterarStatus(Integer id, EventoPedido eventoPedido) {
        try {
            if (EventoPedido.PEDIDO_EM_PREPARO.equals(eventoPedido)) {
                return pedidoClient.iniciarPreparo(id);
            }

            return pedidoClient.finalizarPreparo(id);
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new BaseException(ErrorEnum.PEDIDO_NAO_ENCONTRADO);
            }

            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.TRANSICAO_STATUS_INVALIDA);
            }

            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private void registrarEvento(EventoPedido eventoPedido) {
        notificacaoService.criarNotificacao(new CriarNotificacaoRequest(
                eventoPedido.tipoNotificacao(),
                eventoPedido.destino(),
                null,
                null,
                null
        ));
    }

    private PedidoFilaCozinhaResponse mapearPedido(
            PedidoCozinhaResponse pedido,
            Map<Integer, Integer> numerosMesas,
            Map<Integer, UsuarioClientResponse> usuarios
    ) {
        return new PedidoFilaCozinhaResponse(
                pedido.id(),
                pedido.codigo(),
                pedido.canal(),
                pedido.status(),
                pedido.idMesa(),
                resolverOrigemOperacional(pedido, numerosMesas, usuarios),
                pedido.idAtendimento(),
                pedido.dataCriacao(),
                pedido.dataInicioPreparo(),
                pedido.dataPronto(),
                listarItensDoPedido(pedido).stream()
                        .map(this::mapearItem)
                        .toList()
        );
    }

    private String resolverOrigemOperacional(
            PedidoCozinhaResponse pedido,
            Map<Integer, Integer> numerosMesas,
            Map<Integer, UsuarioClientResponse> usuarios
    ) {
        Integer numeroMesa = pedido.idMesa() == null ? null : numerosMesas.get(pedido.idMesa());

        if (numeroMesa != null) {
            return String.format("Mesa %02d", numeroMesa);
        }

        if ("TOTEM".equalsIgnoreCase(pedido.canal())) {
            UsuarioClientResponse usuario = usuarios.get(pedido.idUsuario());

            if (usuario != null && usuario.nome() != null && !usuario.nome().isBlank()) {
                return usuario.nome().trim();
            }

            return "Totem";
        }

        return pedido.canal();
    }

    private List<ItemPedidoCozinhaResponse> listarItensDoPedido(PedidoCozinhaResponse pedido) {
        if (pedido.itens() == null) {
            return List.of();
        }

        return pedido.itens();
    }

    private ItemFilaCozinhaResponse mapearItem(ItemPedidoCozinhaResponse item) {
        return new ItemFilaCozinhaResponse(
                item.id(),
                item.idProduto(),
                item.nomeProduto(),
                item.quantidade(),
                item.precoUnitario(),
                item.observacao(),
                item.status()
        );
    }

    private PedidoStatusCozinhaResponse mapearStatus(PedidoResponse pedido) {
        return new PedidoStatusCozinhaResponse(
                pedido.id(),
                pedido.codigo(),
                pedido.canal(),
                pedido.status(),
                pedido.idMesa(),
                pedido.idAtendimento()
        );
    }

    public SinalizarProblemaResponse sinalizarProblema(SinalizarProblemaRequest request) {
        try {
            SinalizarProblemaResponse response = pedidoClient.sinalizarProblema(request);

            if (request.statusProdutoPedido().equals(StatusProdutoPedido.FALTA_PRODUTO)){
                registrarEvento(EventoPedido.PEDIDO_COM_FALTA);
            } else if (request.statusProdutoPedido().equals(StatusProdutoPedido.ERRO)) {
                registrarEvento(EventoPedido.PEDIDO_ERRO);
            }else {
                registrarEvento(EventoPedido.PEDIDO_INDISPONIVEL);
            }


            return response;
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new BaseException(ErrorEnum.PEDIDO_NAO_ENCONTRADO);
            }

            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.PEDIDO_NAO_PODE_SINALIZAR_PROBLEMA);
            }

            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    public void decisaoProblema(DecisaoProblemaRequest decisaoProblemaRequest) {
        decisaoProblemaService.registrar(decisaoProblemaRequest);
    }
}
