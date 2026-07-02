package br.com.fourkitchen.bff_restaurante.client.mesas.dto;

public record SessaoMesaResponse(
        Integer idMesa,
        Integer idAtendimento,
        Integer codigoSessao,
        Integer idGarcom,
        String status
) {
}
