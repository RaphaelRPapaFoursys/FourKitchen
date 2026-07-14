package br.com.fourkitchen.bff_restaurante.dto.response;

public record CategoriaCardapioResumoResponse(
        Integer categoriaId,
        String categoriaNome,
        String categoriaDescricao,
        String imagemUrl
) {
}
