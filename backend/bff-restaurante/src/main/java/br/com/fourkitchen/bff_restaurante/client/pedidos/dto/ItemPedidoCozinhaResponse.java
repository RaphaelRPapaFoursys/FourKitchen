package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import java.math.BigDecimal;

public record ItemPedidoCozinhaResponse(
        Integer id,
        Integer idProduto,
        String nomeProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        String observacao,
        String status
) {
}
