package br.com.fourkitchen.ms_pedidos.dto.request;

import java.math.BigDecimal;

public record ProdutoPedidoRequest(
        Integer idProduto,
        String nomeProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        String observacao
) {
}
