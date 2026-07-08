package br.com.fourkitchen.bff_restaurante.client.produtos.dto;

import java.math.BigDecimal;

public record ProdutoDisponibilidadeResponse(
        Integer produtoId,
        String nome,
        Boolean disponivel,
        BigDecimal preco
) {
}
