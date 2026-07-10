package br.com.fourkitchen.bff_restaurante.client.produtos.dto;

import java.math.BigDecimal;

public record ProdutoClientResponse(
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
