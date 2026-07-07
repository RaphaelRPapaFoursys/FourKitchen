package br.com.fourkitchen.ms_usuarios.dto.request;

import br.com.fourkitchen.ms_usuarios.enums.PerfilUsuario;
import br.com.fourkitchen.ms_usuarios.validation.UsuarioRegex;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados necessarios para cadastrar um usuario.")
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
                description = "Senha com no minimo 8 caracteres, uma letra maiuscula, uma letra minuscula e um numero",
                example = "Senha123",
                pattern = UsuarioRegex.SENHA_FORTE,
                minLength = 8
        )
        @NotBlank(message = "Senha e obrigatoria.")
        @Pattern(
                regexp = UsuarioRegex.SENHA_FORTE,
                message = "A senha deve conter no minimo 8 caracteres, uma letra maiuscula, uma letra minuscula e um numero."
        )
        String senha,

        @Schema(description = "Perfil de acesso do usuario", example = "GARCOM")
        @NotNull(message = "Perfil e obrigatorio")
        PerfilUsuario perfilUsuario,

        @Schema(description = "Mesa vinculada ao usuario dispositivo quando perfil for MESA", example = "1")
        @Positive(message = "O campo idMesa deve ser positivo")
        Integer idMesa
) {
}
