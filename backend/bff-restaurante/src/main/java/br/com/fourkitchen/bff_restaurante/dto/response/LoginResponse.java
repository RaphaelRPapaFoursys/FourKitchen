package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de login devolvida pelo BFF ao frontend.")
public record LoginResponse(
        @Schema(description = "JWT emitido pelo ms-usuarios e validado pelo BFF")
        String accessToken,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tokenType,

        @Schema(description = "Dados basicos do usuario autenticado")
        UsuarioAutenticadoResponse usuario
) {
}
