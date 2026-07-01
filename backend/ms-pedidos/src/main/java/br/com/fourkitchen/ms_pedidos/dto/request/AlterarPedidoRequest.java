package br.com.fourkitchen.ms_pedidos.dto.request;

import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;

public record AlterarPedidoRequest(
        CanaisPedido canal,
        StatusPedido status,
        Integer idMesa,
        Integer idUsuario,
        Integer idAtendimento
) {
}
