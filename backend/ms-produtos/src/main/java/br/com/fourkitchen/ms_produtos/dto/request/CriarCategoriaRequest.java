package br.com.fourkitchen.ms_produtos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarCategoriaRequest(
        @NotBlank(message = "Nome da categoria e obrigatorio.")
        @Size(min = 3, max = 80, message = "Nome da categoria deve ter entre 3 e 80 caracteres.")
        String nome
) {
}
