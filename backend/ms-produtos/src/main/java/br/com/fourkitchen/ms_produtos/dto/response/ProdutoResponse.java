package br.com.fourkitchen.ms_produtos.dto.response;

import java.math.BigDecimal;

public record ProdutoResponse(
        Integer id,
        String nome,
        String descricao,
        String imagem,
        BigDecimal preco,
        Integer categoriaId,
        String categoria,
        Boolean disponivel
) {
}
