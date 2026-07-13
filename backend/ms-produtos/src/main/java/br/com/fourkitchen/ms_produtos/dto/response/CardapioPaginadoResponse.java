package br.com.fourkitchen.ms_produtos.dto.response;

import java.util.List;

public record CardapioPaginadoResponse(
        List<CategoriaCardapioResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
