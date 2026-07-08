package br.com.fourkitchen.ms_produtos.dto.response;

import java.math.BigDecimal;

public record ProdutoDisponibilidadeResponse(
        Integer produtoId,
        String nome,
        Boolean disponivel,
        BigDecimal preco
) {
}
