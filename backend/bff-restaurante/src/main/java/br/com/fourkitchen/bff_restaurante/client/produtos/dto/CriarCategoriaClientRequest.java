package br.com.fourkitchen.bff_restaurante.client.produtos.dto;

public record CriarCategoriaClientRequest(
        String nome,
        String descricao,
        String imagem
) {
}
