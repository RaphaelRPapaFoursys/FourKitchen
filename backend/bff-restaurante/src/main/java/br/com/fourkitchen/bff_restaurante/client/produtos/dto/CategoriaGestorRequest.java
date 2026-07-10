package br.com.fourkitchen.bff_restaurante.client.produtos.dto;

public record CategoriaGestorRequest(
        String nome,
        String descricao,
        String imagem
) {
}
