package br.com.fourkitchen.bff_restaurante.client.produtos.dto;

import java.util.List;

public record CardapioPaginadoClientResponse(
        List<CategoriaCardapioClientResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
