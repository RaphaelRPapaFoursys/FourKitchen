package br.com.fourkitchen.ms_pedidos.mapper;

import br.com.fourkitchen.ms_pedidos.dto.response.PedidoResponse;
import br.com.fourkitchen.ms_pedidos.entities.Pedido;
import org.springframework.stereotype.Component;

@Component
public class PedidoResponseMapper implements Mapper<Pedido, PedidoResponse> {
    @Override
    public PedidoResponse map(Pedido pedido) {
        PedidoResponse pedidoResponse = new PedidoResponse(
                pedido.getId(),
                pedido.getCodigo(),
                pedido.getCanal(),
                pedido.getStatus(),
                pedido.getIdMesa(),
                pedido.getIdUsuario()
        );

        return pedidoResponse;
    }
}
