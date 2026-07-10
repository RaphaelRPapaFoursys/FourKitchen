package br.com.fourkitchen.bff_restaurante.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Dados enviados pelo gestor/admin para atualizar um produto.")
public record AtualizarProdutoGestorRequest(
        @Schema(description = "Nome de exibicao do produto", example = "Risoto de cogumelos", minLength = 3, maxLength = 150)
        @NotBlank(message = "Nome e obrigatorio.")
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres.")
        String nome,

        @Schema(description = "Descricao opcional do produto", example = "Arroz arboreo com mix de cogumelos e parmesao", maxLength = 255, nullable = true)
        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres.")
        String descricao,

        @Schema(
                description = "Imagem opcional em Base64 puro ou Data URL Base64. Deve ser JPG/JPEG ou PNG, ate 1 MB, maximo 1200x900 e proporcao 4:3. Quando null, a imagem atual e mantida.",
                example = "iVBORw0KGgoAAAANSUhEUgAA...",
                nullable = true
        )
        String imagem,

        @Schema(description = "Preco atual do produto", example = "58.90", minimum = "0.01")
        @NotNull(message = "Preco e obrigatorio.")
        @DecimalMin(value = "0.01", message = "Preco deve ser maior que zero.")
        BigDecimal preco,

        @Schema(description = "Identificador de uma categoria ativa", example = "1")
        @NotNull(message = "Categoria e obrigatoria.")
        Integer categoriaId
) {
}
