package br.com.fourkitchen.bff_restaurante.client.mesas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricoAtendimentoClientResponse(
        Integer id,
        Integer idAtendimento,
        Integer codigoSessao,
        Integer idMesa,
        Integer numeroMesa,
        Integer idGarcom,
        String nomeGarcom,
        BigDecimal valorFinal,
        Integer totalPedidos,
        Integer totalItens,
        LocalDateTime dataAbertura,
        LocalDateTime dataFechamento,
        Integer duracaoMinutos
) {
}
