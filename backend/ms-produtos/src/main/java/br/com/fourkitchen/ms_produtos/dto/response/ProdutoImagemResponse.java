package br.com.fourkitchen.ms_produtos.dto.response;

public record ProdutoImagemResponse(
        byte[] bytes,
        String contentType
) {
}
