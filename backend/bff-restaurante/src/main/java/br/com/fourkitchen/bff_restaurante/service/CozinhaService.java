package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.AlterarPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.AlterarStatusPedidoCozinhaRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.ItemFilaCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoFilaCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CozinhaService {

    private static final Set<String> STATUS_PERMITIDOS = Set.of("EM_PREPARO", "PRONTO");

    private final PedidoClient pedidoClient;
    private final ProdutoClient produtoClient;

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

    public void alterarStatus(Integer id, AlterarStatusPedidoCozinhaRequest request) {
        String status = request.status().trim().toUpperCase();

        if (!STATUS_PERMITIDOS.contains(status)) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }

        try {
            pedidoClient.alterarPedido(id, new AlterarPedidoRequest(null, status, null, null, null));
        } catch (FeignException.BadRequest | FeignException.NotFound e) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
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
}
