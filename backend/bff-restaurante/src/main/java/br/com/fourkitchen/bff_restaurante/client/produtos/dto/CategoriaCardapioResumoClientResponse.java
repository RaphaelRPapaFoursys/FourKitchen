package br.com.fourkitchen.bff_restaurante.client.produtos.dto;

public record CategoriaCardapioResumoClientResponse(
        Integer categoriaId,
        String categoriaNome,
        String categoriaDescricao,
        String imagemUrl
) {
}
