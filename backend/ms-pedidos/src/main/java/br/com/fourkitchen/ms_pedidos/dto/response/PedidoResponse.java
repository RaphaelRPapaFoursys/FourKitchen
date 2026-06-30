package br.com.fourkitchen.ms_pedidos.dto.response;

import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;


public record PedidoResponse(
        Integer id,
        Integer codigo,
        CanaisPedido canal,
        StatusPedido status,
        Integer idMesa,
        Integer idUsuario
) {
}
