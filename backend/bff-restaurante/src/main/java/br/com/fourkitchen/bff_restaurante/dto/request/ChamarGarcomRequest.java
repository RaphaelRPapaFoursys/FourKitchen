package br.com.fourkitchen.bff_restaurante.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Dados enviados pelo tablet da mesa para chamar o garcom responsavel.")
public record ChamarGarcomRequest(
        @Schema(description = "Identificador da mesa", example = "1")
        @NotNull(message = "O campo idMesa nao pode ser nulo")
        @Positive(message = "O campo idMesa deve ser positivo")
        Integer idMesa,

        @Schema(description = "Codigo da sessao aberta para a mesa", example = "123456")
        @NotNull(message = "O campo codigoSessao nao pode ser nulo")
        @Positive(message = "O campo codigoSessao deve ser positivo")
        Integer codigoSessao
) {
}
