package br.com.fourkitchen.ms_mesas.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AtribuirGarcomRequest(
        @NotNull(message = "Garcom e obrigatorio")
        @Positive(message = "Garcom deve ser um identificador valido")
        Integer garcomId
) {
}
