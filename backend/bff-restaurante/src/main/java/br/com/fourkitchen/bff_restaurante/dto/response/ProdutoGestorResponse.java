package br.com.fourkitchen.bff_restaurante.dto.response;

import java.math.BigDecimal;

public record ProdutoGestorResponse(
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
