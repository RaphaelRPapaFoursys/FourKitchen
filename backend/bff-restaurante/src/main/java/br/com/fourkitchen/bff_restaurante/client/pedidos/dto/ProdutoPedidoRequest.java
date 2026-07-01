package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import java.math.BigDecimal;

public record ProdutoPedidoRequest(
        Integer idProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        String observacao
) {
}
