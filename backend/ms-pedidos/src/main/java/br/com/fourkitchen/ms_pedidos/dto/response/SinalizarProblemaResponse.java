package br.com.fourkitchen.ms_pedidos.dto.response;

import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusProdutoPedido;

public record SinalizarProblemaResponse(
        Integer idPedido,
        Integer idProdutoPedido,
        StatusPedido statusPedido,
        StatusProdutoPedido statusProdutoPedido,
        Integer idMesa,
        Integer idAtendimento
) {
}
