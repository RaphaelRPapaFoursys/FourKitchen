package br.com.fourkitchen.bff_restaurante.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Dados para atualizar produto pelo gestor.")
public record AtualizarProdutoGestorRequest(
        @Schema(description = "Nome do produto", example = "X-Burger")
        @NotBlank(message = "Nome e obrigatorio.")
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres.")
        String nome,

        @Schema(description = "Descricao do produto", example = "Pao, carne, queijo e molho da casa", nullable = true)
        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres.")
        String descricao,

        @Schema(
                description = "Nova imagem opcional em Base64 puro ou Data URL. Quando null, mantem a imagem atual.",
                example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
                nullable = true
        )
        String imagem,

        @Schema(description = "Preco do produto", example = "29.90")
        @NotNull(message = "Preco e obrigatorio.")
        @DecimalMin(value = "0.01", message = "Preco deve ser maior que zero.")
        BigDecimal preco,

        @Schema(description = "Identificador da categoria", example = "1")
        @NotNull(message = "Categoria e obrigatoria.")
        Integer categoriaId
) {
}
