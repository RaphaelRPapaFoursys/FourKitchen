package br.com.fourkitchen.bff_restaurante.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados enviados pelo gestor/admin para cadastrar uma categoria.")
public record CriarCategoriaGestorRequest(
        @Schema(description = "Nome unico da categoria", example = "Pratos principais", minLength = 3, maxLength = 80)
        @NotBlank(message = "Nome da categoria e obrigatorio.")
        @Size(min = 3, max = 80, message = "Nome da categoria deve ter entre 3 e 80 caracteres.")
        String nome,

        @Schema(description = "Descricao opcional da categoria", example = "Refeicoes principais servidas no restaurante", maxLength = 255, nullable = true)
        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres.")
        String descricao,

        @Schema(
                description = "Imagem opcional em Base64 puro ou Data URL Base64. Deve ser JPG/JPEG ou PNG, ate 1 MB, maximo 1200x900 e proporcao 4:3.",
                example = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ...",
                nullable = true
        )
        String imagem
) {
}
