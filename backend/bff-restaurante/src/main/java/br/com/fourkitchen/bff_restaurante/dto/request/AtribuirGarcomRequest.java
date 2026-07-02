package br.com.fourkitchen.bff_restaurante.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AtribuirGarcomRequest(
        @NotNull(message = "Garcom e obrigatorio")
        @Positive(message = "Garcom deve ser um identificador valido")
        Integer garcomId
) {
}
