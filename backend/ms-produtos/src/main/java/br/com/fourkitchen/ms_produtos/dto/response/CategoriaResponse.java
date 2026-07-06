package br.com.fourkitchen.ms_produtos.dto.response;

public record CategoriaResponse(
        Integer id,
        String nome,
        String descricao,
        Boolean ativo
) {
}
