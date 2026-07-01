package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ProdutoPedidoRequest;
import org.springframework.stereotype.Component;

@Component
public class ItemPedidoTotemRequestMapper implements Mapper<ItemPedidoTotemMapperSource, ProdutoPedidoRequest> {

    @Override
    public ProdutoPedidoRequest map(ItemPedidoTotemMapperSource source) {
        return new ProdutoPedidoRequest(
                source.item().idProduto(),
                source.item().quantidade(),
                source.disponibilidade().preco(),
                source.item().observacao()
        );
    }
}
