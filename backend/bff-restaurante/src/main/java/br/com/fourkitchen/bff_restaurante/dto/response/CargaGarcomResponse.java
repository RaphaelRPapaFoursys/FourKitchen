package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Carga de mesas ativas por garcom.")
public record CargaGarcomResponse(
        @Schema(description = "Identificador do garcom", example = "7")
        Integer id,

        @Schema(description = "Nome do garcom", example = "Amanda Souza")
        String nome,

        @Schema(description = "Quantidade de mesas ocupadas atribuidas ao garcom", example = "3")
        Integer mesasAtivas
) {
}
