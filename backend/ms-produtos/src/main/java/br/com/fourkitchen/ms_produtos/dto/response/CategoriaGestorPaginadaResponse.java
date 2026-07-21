package br.com.fourkitchen.ms_produtos.dto.response;

import java.util.List;

public record CategoriaGestorPaginadaResponse(
        List<CategoriaResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
