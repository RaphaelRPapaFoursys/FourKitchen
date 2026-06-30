package br.com.fourkitchen.ms_pedidos.mapper;

import br.com.fourkitchen.ms_pedidos.dto.response.ItemPedidoResponse;
import br.com.fourkitchen.ms_pedidos.entities.ItemPedido;
import org.springframework.stereotype.Component;

@Component
public class ItemPedidoResponseMapper implements Mapper<ItemPedido, ItemPedidoResponse> {
    @Override
    public ItemPedidoResponse map(ItemPedido itemPedido) {
        ItemPedidoResponse itemPedidoResponse = new ItemPedidoResponse(
                itemPedido.getId(),
                itemPedido.getIdPedido(),
                itemPedido.getIdProduto()
        );

        return itemPedidoResponse;
    }
}