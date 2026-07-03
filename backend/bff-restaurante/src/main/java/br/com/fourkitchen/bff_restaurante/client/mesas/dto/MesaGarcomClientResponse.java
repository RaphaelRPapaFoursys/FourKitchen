package br.com.fourkitchen.bff_restaurante.client.mesas.dto;

import java.time.LocalDateTime;

public record MesaGarcomClientResponse(
        Integer idMesa,
        Integer numero,
        String status,
        Integer idAtendimento,
        Integer codigoSessao,
        Integer idGarcom,
        LocalDateTime dataAbertura
) {
}
