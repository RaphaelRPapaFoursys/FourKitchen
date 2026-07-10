package br.com.fourkitchen.bff_restaurante.client.produtos.dto;

import java.math.BigDecimal;

public record CriarProdutoClientRequest(
        String nome,
        String descricao,
        String imagem,
        BigDecimal preco,
        Integer categoriaId,
        Boolean disponivel
) {
}
