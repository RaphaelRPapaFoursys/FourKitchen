package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Garcom disponivel para atribuicao de mesas.")
public record GarcomResumoResponse(
        @Schema(description = "Identificador do garcom", example = "7")
        Integer id,

        @Schema(description = "Nome do garcom", example = "Amanda Souza")
        String nome,

        @Schema(description = "Email do garcom", example = "amanda@fourkitchen.com")
        String email
) {
}
