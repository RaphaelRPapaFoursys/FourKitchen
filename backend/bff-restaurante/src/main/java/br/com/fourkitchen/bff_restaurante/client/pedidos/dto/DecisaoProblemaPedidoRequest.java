package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import br.com.fourkitchen.bff_restaurante.enums.StatusProdutoPedido;

public record DecisaoProblemaPedidoRequest(
        Integer idPedido,
        Integer idProdutoPedido,
        StatusProdutoPedido novoStatusProdutoPedido,
        Boolean pedidoCancelado,
        Integer idNovoProduto,
        String nomeNovoProduto
) {
}
