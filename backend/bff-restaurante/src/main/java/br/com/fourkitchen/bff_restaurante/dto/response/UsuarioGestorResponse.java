package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuario retornado para o painel de gestao.")
public record UsuarioGestorResponse(
        @Schema(description = "Identificador do usuario", example = "1")
        Integer id,

        @Schema(description = "Nome do usuario", example = "Maria Silva")
        String nome,

        @Schema(description = "Email do usuario", example = "maria@fourkitchen.com")
        String email,

        @Schema(description = "Perfil de acesso do usuario", example = "GARCOM")
        String perfilUsuario,

        @Schema(description = "Mesa vinculada ao usuario dispositivo quando perfil for MESA", example = "1")
        Integer idMesa,

        @Schema(description = "Indica se o usuario esta ativo", example = "true")
        Boolean ativo
) {
}
