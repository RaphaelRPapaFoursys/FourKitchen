package br.com.fourkitchen.ms_mesas.dto.response;

import br.com.fourkitchen.ms_mesas.enums.StatusMesa;

public record SessaoMesaResponse(
        Integer idMesa,
        Integer idAtendimento,
        Integer codigoSessao,
        Integer idGarcom,
        StatusMesa status
) {
}
