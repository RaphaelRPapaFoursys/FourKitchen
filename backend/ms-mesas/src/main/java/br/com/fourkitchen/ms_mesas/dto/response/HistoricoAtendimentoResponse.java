package br.com.fourkitchen.ms_mesas.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricoAtendimentoResponse(
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
