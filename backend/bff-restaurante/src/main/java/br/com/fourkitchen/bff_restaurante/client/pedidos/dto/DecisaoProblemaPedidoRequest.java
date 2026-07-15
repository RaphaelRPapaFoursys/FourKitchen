package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import br.com.fourkitchen.bff_restaurante.enums.StatusProdutoPedido;

import java.math.BigDecimal;

public record DecisaoProblemaPedidoRequest(
        Integer idPedido,
        Integer idProdutoPedido,
        StatusProdutoPedido novoStatusProdutoPedido,
        Boolean pedidoCancelado,
        Integer idNovoProduto,
        String nomeNovoProduto,
        BigDecimal precoNovoProduto,
        String observacaoNovoProduto
) {
    public DecisaoProblemaPedidoRequest(
            Integer idPedido,
            Integer idProdutoPedido,
            StatusProdutoPedido novoStatusProdutoPedido,
            Boolean pedidoCancelado,
            Integer idNovoProduto,
            String nomeNovoProduto,
            BigDecimal precoNovoProduto
    ) {
        this(
                idPedido,
                idProdutoPedido,
                novoStatusProdutoPedido,
                pedidoCancelado,
                idNovoProduto,
                nomeNovoProduto,
                precoNovoProduto,
                null
        );
    }

    public DecisaoProblemaPedidoRequest(
            Integer idPedido,
            Integer idProdutoPedido,
            StatusProdutoPedido novoStatusProdutoPedido,
            Boolean pedidoCancelado,
            Integer idNovoProduto,
            String nomeNovoProduto
    ) {
        this(
                idPedido,
                idProdutoPedido,
                novoStatusProdutoPedido,
                pedidoCancelado,
                idNovoProduto,
                nomeNovoProduto,
                null,
                null
        );
    }
}
