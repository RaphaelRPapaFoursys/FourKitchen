package br.com.fourkitchen.bff_restaurante.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AlterarStatusPedidoCozinhaRequest(
        @NotBlank
        String status
) {
}
