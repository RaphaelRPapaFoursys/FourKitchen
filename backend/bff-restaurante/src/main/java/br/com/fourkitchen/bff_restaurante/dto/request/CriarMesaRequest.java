package br.com.fourkitchen.bff_restaurante.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CriarMesaRequest(
        @NotNull(message = "Numero da mesa e obrigatorio")
        @Positive(message = "Numero da mesa deve ser maior que zero")
        Integer numero
) {
}
