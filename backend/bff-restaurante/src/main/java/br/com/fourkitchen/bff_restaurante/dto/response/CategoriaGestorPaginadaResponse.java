package br.com.fourkitchen.bff_restaurante.dto.response;

import java.util.List;

public record CategoriaGestorPaginadaResponse(
        List<CategoriaGestorResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
