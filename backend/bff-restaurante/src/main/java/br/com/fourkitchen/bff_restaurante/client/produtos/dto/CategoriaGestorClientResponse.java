package br.com.fourkitchen.bff_restaurante.client.produtos.dto;

public record CategoriaGestorClientResponse(
        Integer id,
        String nome,
        String descricao,
        String imagemUrl,
        Boolean ativo
) {
}
