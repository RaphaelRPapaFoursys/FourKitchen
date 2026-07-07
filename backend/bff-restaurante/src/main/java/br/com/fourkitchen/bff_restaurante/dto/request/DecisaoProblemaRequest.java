package br.com.fourkitchen.bff_restaurante.dto.request;

import br.com.fourkitchen.bff_restaurante.enums.StatusProdutoPedido;

public record DecisaoProblemaRequest(
        Integer idPedido,
        Integer idProdutoPedido,
        StatusProdutoPedido novoStatusProdutoPedido,
        Boolean pedidoCancelado,
        Integer idNovoProduto
) {
}