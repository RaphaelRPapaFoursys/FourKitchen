package br.com.fourkitchen.ms_pedidos.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssumirProblemaTotemRequest(
        @NotNull @Positive Integer idGarcom
) {
}
