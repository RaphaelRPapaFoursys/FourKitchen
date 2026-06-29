package br.com.fourkitchen.ms_produtos.dto.response;

public record CategoriaResponse(
        Integer id,
        String nome,
        Boolean ativo
) {
}
