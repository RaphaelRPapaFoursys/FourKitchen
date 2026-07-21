package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Produto retornado para gerenciamento do gestor/admin.")
public record ProdutoGestorResponse(
        @Schema(description = "Identificador do produto", example = "10")
        Integer id,

        @Schema(description = "Nome do produto", example = "Risoto de cogumelos")
        String nome,

        @Schema(description = "Descricao do produto", example = "Arroz arboreo com mix de cogumelos e parmesao", nullable = true)
        String descricao,

        @Schema(description = "URL versionada da imagem do produto", example = "/api/produtos/10/imagem?v=1784635200000", nullable = true)
        String imagemUrl,

        @Schema(description = "Preco atual do produto", example = "58.90")
        BigDecimal preco,

        @Schema(description = "Identificador da categoria vinculada", example = "1")
        Integer categoriaId,

        @Schema(description = "Nome da categoria vinculada", example = "Pratos principais")
        String categoria,

        @Schema(description = "Indica se o produto esta disponivel para venda", example = "true")
        Boolean disponivel
) {
}
