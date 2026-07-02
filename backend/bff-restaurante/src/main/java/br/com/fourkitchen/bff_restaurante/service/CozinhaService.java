package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoResponse;
import br.com.fourkitchen.bff_restaurante.dto.EventoPedido;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.ItemFilaCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoFilaCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoStatusCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CozinhaService {

    private final PedidoClient pedidoClient;
    private final ProdutoClient produtoClient;
    private final NotificacaoService notificacaoService;

    public List<PedidoFilaCozinhaResponse> listarFila() {
        try {
            List<PedidoCozinhaResponse> pedidos = pedidoClient.listarFilaCozinha();
            Map<Integer, String> nomesProdutos = buscarNomesProdutosSemBloquearFila();

            return pedidos
                    .stream()
                    .map(pedido -> mapearPedido(pedido, nomesProdutos))
                    .toList();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
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

    private PedidoFilaCozinhaResponse mapearPedido(PedidoCozinhaResponse pedido, Map<Integer, String> nomesProdutos) {
        return new PedidoFilaCozinhaResponse(
                pedido.id(),
                pedido.codigo(),
                pedido.canal(),
                pedido.status(),
                pedido.idMesa(),
                pedido.idAtendimento(),
                pedido.dataCriacao(),
                itensDoPedido(pedido).stream()
                        .map(item -> mapearItem(item, nomesProdutos))
                        .toList()
        );
    }

    private List<ItemPedidoCozinhaResponse> itensDoPedido(PedidoCozinhaResponse pedido) {
        if (pedido.itens() == null) {
            return List.of();
        }

        return pedido.itens();
    }

    private ItemFilaCozinhaResponse mapearItem(ItemPedidoCozinhaResponse item, Map<Integer, String> nomesProdutos) {
        return new ItemFilaCozinhaResponse(
                item.id(),
                item.idProduto(),
                nomesProdutos.getOrDefault(item.idProduto(), "Produto #" + item.idProduto()),
                item.quantidade(),
                item.precoUnitario(),
                item.observacao()
        );
    }

    private Map<Integer, String> buscarNomesProdutosSemBloquearFila() {
        try {
            return produtoClient.listarProdutos()
                    .stream()
                    .filter(produto -> produto.id() != null)
                    .collect(Collectors.toMap(
                            ProdutoResponse::id,
                            ProdutoResponse::nome,
                            (nomeAtual, nomeNovo) -> nomeAtual
                    ));
        } catch (Exception e) {
            return Map.of();
        }
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
}
