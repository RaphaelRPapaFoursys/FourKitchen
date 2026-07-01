package br.com.fourkitchen.ms_pedidos.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CriarProdutoPedidoRequest(
        @NotNull
        Integer quantidade,
        @NotNull
        Integer idPedido,
        @NotNull
        Integer idProduto,
        @NotNull
        BigDecimal precoUnitario,
        String observacao
) {
}
