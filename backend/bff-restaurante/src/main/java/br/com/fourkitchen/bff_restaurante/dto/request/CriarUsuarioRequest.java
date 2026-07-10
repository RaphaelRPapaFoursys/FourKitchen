package br.com.fourkitchen.bff_restaurante.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados enviados pelo front para cadastrar um usuario pelo gestor/admin.")
public record CriarUsuarioRequest(
        @Schema(description = "Nome do usuario", example = "Maria Silva", minLength = 3, maxLength = 120)
        @NotBlank(message = "Nome e obrigatorio.")
        @Size(min = 3, max = 120, message = "Nome deve ter entre 3 e 120 caracteres.")
        String nome,

        @Schema(description = "Email unico do usuario", example = "maria@fourkitchen.com")
        @NotBlank(message = "Email e obrigatorio.")
        @Email(message = "Email invalido")
        String email,

        @Schema(
                description = "Senha com no minimo 8 caracteres, uma letra maiuscula, uma letra minuscula e um numero.",
                example = "Senha123",
                minLength = 8
        )
        @NotBlank(message = "Senha e obrigatoria.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "A senha deve conter no minimo 8 caracteres, uma letra maiuscula, uma letra minuscula e um numero."
        )
        String senha,

        @Schema(description = "Perfil de acesso do usuario", example = "GARCOM")
        @NotBlank(message = "Perfil e obrigatorio")
        String perfilUsuario,

        @Schema(
                description = "Mesa vinculada ao usuario dispositivo. Obrigatorio quando perfilUsuario for MESA; deve ser null para outros perfis.",
                example = "1",
                nullable = true
        )
        @Positive(message = "O campo idMesa deve ser positivo")
        Integer idMesa
) {
}
