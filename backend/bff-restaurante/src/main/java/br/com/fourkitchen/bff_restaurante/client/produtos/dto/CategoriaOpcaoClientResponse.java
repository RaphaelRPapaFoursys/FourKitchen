package br.com.fourkitchen.bff_restaurante.client.produtos.dto;

public record CategoriaOpcaoClientResponse(
        Integer id,
        String nome,
        Boolean ativo
) {
}
