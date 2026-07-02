package br.com.fourkitchen.ms_pedidos.dto.response;

import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoCozinhaResponse(
        Integer id,
        Integer codigo,
        CanaisPedido canal,
        StatusPedido status,
        Integer idMesa,
        Integer idAtendimento,
        LocalDateTime dataCriacao,
        List<ItemPedidoCozinhaResponse> itens
) {
}
