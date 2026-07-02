package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ItemFilaCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoFilaCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CozinhaService {

    private final PedidoClient pedidoClient;

    public List<PedidoFilaCozinhaResponse> listarFila() {
        try {
            return pedidoClient.listarFilaCozinha()
                    .stream()
                    .map(this::mapearPedido)
                    .toList();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private PedidoFilaCozinhaResponse mapearPedido(PedidoCozinhaResponse pedido) {
        return new PedidoFilaCozinhaResponse(
                pedido.id(),
                pedido.codigo(),
                pedido.canal(),
                pedido.status(),
                pedido.idMesa(),
                pedido.idAtendimento(),
                pedido.dataCriacao(),
                itensDoPedido(pedido).stream()
                        .map(this::mapearItem)
                        .toList()
        );
    }

    private List<ItemPedidoCozinhaResponse> itensDoPedido(PedidoCozinhaResponse pedido) {
        if (pedido.itens() == null) {
            return List.of();
        }

        return pedido.itens();
    }

    private ItemFilaCozinhaResponse mapearItem(ItemPedidoCozinhaResponse item) {
        return new ItemFilaCozinhaResponse(
                item.id(),
                item.idProduto(),
                item.quantidade(),
                item.precoUnitario(),
                item.observacao()
        );
    }
}
