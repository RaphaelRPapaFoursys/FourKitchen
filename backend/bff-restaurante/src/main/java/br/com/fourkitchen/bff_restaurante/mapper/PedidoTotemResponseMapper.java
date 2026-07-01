package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoTotemResponse;
import org.springframework.stereotype.Component;

@Component
public class PedidoTotemResponseMapper implements Mapper<PedidoResponse, PedidoTotemResponse> {

    @Override
    public PedidoTotemResponse map(PedidoResponse pedido) {
        return new PedidoTotemResponse(
                pedido.id(),
                pedido.codigo(),
                pedido.canal(),
                pedido.status()
        );
    }
}
