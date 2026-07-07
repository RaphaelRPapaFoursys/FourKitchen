package br.com.fourkitchen.ms_pedidos.dto.request;

import br.com.fourkitchen.ms_pedidos.enums.StatusProdutoPedido;

public record DecisaoProblemaRequest(
        Integer idPedido,
        Integer idProdutoPedido,
        StatusProdutoPedido novoStatusProdutoPedido,
        Boolean pedidoCancelado,
        Integer idNovoProduto
) {
}
