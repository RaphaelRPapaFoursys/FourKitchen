package br.com.fourkitchen.ms_usuarios.dto.request;

import br.com.fourkitchen.ms_usuarios.enums.PerfilUsuario;
import jakarta.validation.constraints.*;

public record CriarUsuarioRequest(
        @NotBlank(message = "Nome é obrigatório.")
        @Size(min = 3, max = 120, message = "Nome deve ter entre 3 e 120 caracteres.")
        String nome,

        @NotBlank(message = "Email é obrigatório.")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "A senha deve conter no mínimo 8 caracteres, uma letra maiúscula, uma letra minúscula e um número."
        )
        String senha,

        @NotNull(message = "Perfil é obrigatório")
        PerfilUsuario perfilUsuario
) {
}
