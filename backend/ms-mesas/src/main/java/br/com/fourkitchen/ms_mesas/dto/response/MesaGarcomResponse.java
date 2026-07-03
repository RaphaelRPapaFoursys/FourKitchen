package br.com.fourkitchen.ms_mesas.dto.response;

import br.com.fourkitchen.ms_mesas.enums.StatusMesa;

import java.time.LocalDateTime;

public record MesaGarcomResponse(
        Integer idMesa,
        Integer numero,
        StatusMesa status,
        Integer idAtendimento,
        Integer codigoSessao,
        Integer idGarcom,
        LocalDateTime dataAbertura
) {
}
