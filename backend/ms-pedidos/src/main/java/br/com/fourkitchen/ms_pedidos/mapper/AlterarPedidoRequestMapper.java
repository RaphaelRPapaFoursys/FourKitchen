package br.com.fourkitchen.ms_pedidos.mapper;

import br.com.fourkitchen.ms_pedidos.dto.request.AlterarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.entities.Pedido;
import org.springframework.stereotype.Component;

@Component
public class AlterarPedidoRequestMapper implements Mapper<AlterarPedidoRequest, Pedido> {
    @Override
    public Pedido map(AlterarPedidoRequest alterarPedidoRequest) {
        Pedido pedido = Pedido
                .builder()
                .canal(alterarPedidoRequest.canal())
                .status(alterarPedidoRequest.status())
                .idMesa(alterarPedidoRequest.idMesa())
                .idUsuario(alterarPedidoRequest.idUsuario())
                .idAtendimento(alterarPedidoRequest.idAtendimento())
                .build();

        return pedido;
    }
}
