package br.com.fourkitchen.bff_restaurante.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados de login enviados pelo frontend ao BFF.")
public record LoginRequest(
        @Schema(description = "Email do usuario", example = "garcom@fourkitchen.com")
        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email invalido")
        String email,

        @Schema(description = "Senha do usuario", example = "Senha123")
        @NotBlank(message = "Senha e obrigatoria")
        String senha
) {
}
