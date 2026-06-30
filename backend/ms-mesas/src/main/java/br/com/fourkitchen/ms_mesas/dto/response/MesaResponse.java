package br.com.fourkitchen.ms_mesas.dto.response;

import br.com.fourkitchen.ms_mesas.enums.StatusMesa;

import java.time.LocalDateTime;

public record MesaResponse(
        Integer id,
        Integer numero,
        StatusMesa status,
        Integer garcomId,
        Integer codigoSessao,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento
) {
}
