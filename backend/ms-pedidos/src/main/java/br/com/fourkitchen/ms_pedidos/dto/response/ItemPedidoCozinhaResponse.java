package br.com.fourkitchen.ms_pedidos.dto.response;

import java.math.BigDecimal;

public record ItemPedidoCozinhaResponse(
        Integer id,
        Integer idProduto,
        String nomeProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        String observacao
) {
}
