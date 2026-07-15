package br.com.fourkitchen.ms_pedidos.dto.request;

import br.com.fourkitchen.ms_pedidos.enums.StatusProdutoPedido;

import java.math.BigDecimal;

public record DecisaoProblemaRequest(
        Integer idPedido,
        Integer idProdutoPedido,
        StatusProdutoPedido novoStatusProdutoPedido,
        Boolean pedidoCancelado,
        Integer idNovoProduto,
        String nomeNovoProduto,
        BigDecimal precoNovoProduto,
        String observacaoNovoProduto
) {
    public DecisaoProblemaRequest(
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

    public DecisaoProblemaRequest(
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
