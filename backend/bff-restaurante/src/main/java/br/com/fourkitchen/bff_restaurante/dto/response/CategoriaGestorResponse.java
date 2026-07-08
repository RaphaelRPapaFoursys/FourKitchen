package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoria exibida no gerenciamento do gestor.")
public record CategoriaGestorResponse(
        @Schema(description = "Identificador da categoria", example = "1")
        Integer id,

        @Schema(description = "Nome da categoria", example = "Lanches")
        String nome,

        @Schema(description = "Descricao da categoria", example = "Hamburgueres e sanduiches", nullable = true)
        String descricao,

        @Schema(description = "Imagem da categoria em Base64", example = "iVBORw0KGgoAAAANSUhEUgAA...", nullable = true)
        String imagem,

        @Schema(description = "Indica se a categoria esta ativa", example = "true")
        Boolean ativo
) {
}
