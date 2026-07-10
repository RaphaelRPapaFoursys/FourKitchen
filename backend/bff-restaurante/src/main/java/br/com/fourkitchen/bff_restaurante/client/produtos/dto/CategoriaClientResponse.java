package br.com.fourkitchen.bff_restaurante.client.produtos.dto;

public record CategoriaClientResponse(
        Integer id,
        String nome,
        String descricao,
        Boolean ativo
) {
}
