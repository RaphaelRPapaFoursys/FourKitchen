package br.com.fourkitchen.bff_restaurante.dto.response;

public record CategoriaGestorResponse(
        Integer id,
        String nome,
        String descricao,
        Boolean ativo
) {
}
