package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados do usuario autenticado extraidos do JWT.")
public record UsuarioAutenticadoResponse(
        @Schema(description = "Identificador do usuario", example = "1")
        Long id,

        @Schema(description = "Nome do usuario", example = "Lucas")
        String nome,

        @Schema(description = "Email do usuario", example = "garcom@fourkitchen.com")
        String email,

        @Schema(description = "Perfil do usuario", example = "GARCOM")
        String perfil
) {
}
