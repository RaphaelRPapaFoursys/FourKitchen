package br.com.fourkitchen.bff_restaurante.dto.response;

import java.time.LocalDateTime;

public record MesaResponse(
        Integer id,
        Integer numero,
        String status,
        Integer garcomId,
        Integer codigoSessao,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento
) {
}
