package br.com.fourkitchen.ms_produtos.dto.response;

public record CategoriaImagemResponse(
        byte[] bytes,
        String contentType
) {
}
