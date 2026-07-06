package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Categoria do cardapio com seus produtos disponiveis.")
public record CategoriaCardapioResponse(
        @Schema(description = "Identificador da categoria", example = "1")
        Integer categoriaId,

        @Schema(description = "Nome da categoria", example = "Lanches")
        String categoriaNome,

        @Schema(description = "Descricao da categoria", example = "Sanduiches, porcoes e combinados", nullable = true)
        String categoriaDescricao,

        @Schema(description = "Produtos disponiveis da categoria")
        List<ProdutoCardapioResponse> produtos
) {
}
