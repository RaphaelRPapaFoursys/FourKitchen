package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Pagina de produtos do cardapio agrupados por categoria.")
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
