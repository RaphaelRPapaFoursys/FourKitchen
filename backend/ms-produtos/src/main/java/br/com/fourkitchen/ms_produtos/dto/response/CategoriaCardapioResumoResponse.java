package br.com.fourkitchen.ms_produtos.dto.response;

public record CategoriaCardapioResumoResponse(
        Integer categoriaId,
        String categoriaNome,
        String categoriaDescricao
) {
}
