package br.com.fourkitchen.ms_pedidos.mapper;

import br.com.fourkitchen.ms_pedidos.dto.response.ProdutoPedidoResponse;
import br.com.fourkitchen.ms_pedidos.entities.ProdutoPedido;
import org.springframework.stereotype.Component;

@Component
public class ProdutoPedidoResponseMapper implements Mapper<ProdutoPedido, ProdutoPedidoResponse> {
    @Override
    public ProdutoPedidoResponse map(ProdutoPedido itemPedido) {
        ProdutoPedidoResponse produtoPedidoResponse = new ProdutoPedidoResponse(
                itemPedido.getId(),
                itemPedido.getIdPedido(),
                itemPedido.getIdProduto(),
                itemPedido.getNomeProduto()
        );

        return produtoPedidoResponse;
    }
}
