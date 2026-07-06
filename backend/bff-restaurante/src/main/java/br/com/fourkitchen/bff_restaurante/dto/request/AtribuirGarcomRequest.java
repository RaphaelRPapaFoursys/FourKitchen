package br.com.fourkitchen.bff_restaurante.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Dados para atribuir um garcom a uma mesa ocupada.")
public record AtribuirGarcomRequest(
        @Schema(description = "Identificador do garcom que assumira a mesa", example = "7")
        @NotNull(message = "Garcom e obrigatorio.")
        @Positive(message = "Garcom deve ser um identificador valido.")
        Integer garcomId
) {
}
