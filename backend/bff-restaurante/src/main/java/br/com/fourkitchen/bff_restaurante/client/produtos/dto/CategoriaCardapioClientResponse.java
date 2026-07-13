package br.com.fourkitchen.bff_restaurante.client.produtos.dto;

import java.util.List;

public record CategoriaCardapioClientResponse(
        Integer categoriaId,
        String categoriaNome,
        String categoriaDescricao,
        String categoriaImagem,
        List<ProdutoCardapioClientResponse> produtos
) {
}
