package br.com.fourkitchen.bff_restaurante.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastrar categoria pelo gestor.")
public record CriarCategoriaGestorRequest(
        @Schema(description = "Nome da categoria", example = "Lanches")
        @NotBlank(message = "Nome da categoria e obrigatorio.")
        @Size(min = 3, max = 80, message = "Nome da categoria deve ter entre 3 e 80 caracteres.")
        String nome,

        @Schema(description = "Descricao da categoria", example = "Hamburgueres e sanduiches", nullable = true)
        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres.")
        String descricao,

        @Schema(
                description = "Imagem opcional em Base64 puro ou Data URL. Aceita JPG/JPEG/PNG, ate 1 MB, maximo 1200x900 e proporcao 4:3.",
                example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
                nullable = true
        )
        String imagem
) {
}
