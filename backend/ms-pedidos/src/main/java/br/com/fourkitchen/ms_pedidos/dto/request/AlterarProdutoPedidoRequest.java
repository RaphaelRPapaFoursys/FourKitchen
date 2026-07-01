package br.com.fourkitchen.ms_pedidos.dto.request;

import java.math.BigDecimal;

public record AlterarProdutoPedidoRequest(
        Integer quantidade,
        BigDecimal precoUnitario,
        String observacao
) {
}