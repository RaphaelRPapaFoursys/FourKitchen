package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Resposta paginada de mesas do painel do gestor.")
public record MesaGestorPaginadaResponse(
        @Schema(description = "Mesas da pagina atual")
        List<MesaGestorResponse> content,

        @Schema(description = "Pagina atual, iniciando em zero", example = "0")
        Integer page,

        @Schema(description = "Quantidade de itens solicitada por pagina", example = "10")
        Integer size,

        @Schema(description = "Total de mesas encontradas", example = "48")
        Long totalElements,

        @Schema(description = "Total de paginas disponiveis", example = "5")
        Integer totalPages,

        @Schema(description = "Indica se esta e a primeira pagina", example = "true")
        Boolean first,

        @Schema(description = "Indica se esta e a ultima pagina", example = "false")
        Boolean last
) {
}
