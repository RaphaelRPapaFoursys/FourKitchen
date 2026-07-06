package br.com.fourkitchen.bff_restaurante.client.mesas.dto;

import java.time.LocalDateTime;

public record MesaClientResponse(
        Integer id,
        Integer numero,
        String status,
        Integer garcomId,
        Integer codigoSessao,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        Integer idAtendimento
) {
}
