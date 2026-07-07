package br.com.fourkitchen.ms_produtos.dto.response;

import java.math.BigDecimal;

public record ProdutoCardapioResponse(
        Integer id,
        String nome,
        String descricao,
        BigDecimal preco
) {
}
