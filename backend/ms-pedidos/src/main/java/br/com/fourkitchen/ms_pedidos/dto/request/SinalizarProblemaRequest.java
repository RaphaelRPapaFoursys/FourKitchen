package br.com.fourkitchen.ms_pedidos.dto.request;

import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusProdutoPedido;

public record SinalizarProblemaRequest(
        Integer idPedido,
        Integer idProdutoPedido,
        StatusProdutoPedido statusProdutoPedido
) {
}
