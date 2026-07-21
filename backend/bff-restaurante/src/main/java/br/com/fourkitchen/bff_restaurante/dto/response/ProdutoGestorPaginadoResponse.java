package br.com.fourkitchen.bff_restaurante.dto.response;

import java.util.List;

public record ProdutoGestorPaginadoResponse(
        List<ProdutoGestorResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
