package br.com.fourkitchen.ms_pedidos.mapper;

import br.com.fourkitchen.ms_pedidos.dto.request.CriarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.entities.Pedido;
import org.springframework.stereotype.Component;

@Component
public class CriarPedidoRequestMapper implements Mapper<CriarPedidoRequest, Pedido> {

    @Override
    public Pedido map(CriarPedidoRequest pedidoRequest) {
        Pedido pedido = Pedido
                .builder()
                .codigo(pedidoRequest.codigo())
                .canal(pedidoRequest.canal())
                .status(pedidoRequest.status())
                .idMesa(pedidoRequest.idMesa())
                .idUsuario(pedidoRequest.idUsuario())
                .idAtendimento(pedidoRequest.idAtendimento())
                .build();

        return pedido;
    }
}
