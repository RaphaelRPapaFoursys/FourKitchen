package br.com.fourkitchen.ms_produtos.dto.response;

import java.util.List;

public record ProdutoGestorPaginadoResponse(
        List<ProdutoResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
