package br.com.fourkitchen.bff_restaurante.client.produtos.dto;

public record AtualizarCategoriaClientRequest(
        String nome,
        String descricao,
        String imagem
) {
}
