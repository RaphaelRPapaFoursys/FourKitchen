package br.com.fourkitchen.ms_usuarios.dto.response;

import br.com.fourkitchen.ms_usuarios.enums.PerfilUsuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuario retornado pela API.")
public record UsuarioResponse(
        @Schema(description = "Identificador do usuario", example = "1")
        Integer id,

        @Schema(description = "Nome do usuario", example = "Maria Silva")
        String nome,

        @Schema(description = "Email do usuario", example = "maria@fourkitchen.com")
        String email,

        @Schema(description = "Perfil de acesso do usuario", example = "GARCOM")
        PerfilUsuario perfilUsuario,

        @Schema(description = "Indica se o usuario esta ativo", example = "true")
        Boolean ativo
) {
}
