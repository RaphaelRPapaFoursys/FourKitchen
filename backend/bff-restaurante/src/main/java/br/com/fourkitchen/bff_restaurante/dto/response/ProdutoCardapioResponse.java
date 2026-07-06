package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Produto disponivel para exibicao no cardapio.")
public record ProdutoCardapioResponse(
        @Schema(description = "Identificador do produto", example = "10")
        Integer id,

        @Schema(description = "Nome do produto", example = "X-Burger")
        String nome,

        @Schema(description = "Descricao do produto", example = "Pao, carne, queijo e molho da casa", nullable = true)
        String descricao,

        @Schema(description = "Imagem do produto em Base64", example = "iVBORw0KGgoAAAANSUhEUgAA...", nullable = true)
        String imagem,

        @Schema(description = "Preco atual do produto", example = "29.90")
        BigDecimal preco
) {
}
