package br.com.fourkitchen.bff_restaurante.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarCategoriaRequest(
        @NotBlank(message = "Nome da categoria e obrigatorio.")
        @Size(min = 3, max = 80, message = "Nome da categoria deve ter entre 3 e 80 caracteres.")
        String nome,

        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres.")
        String descricao
) {
}
