package br.com.fourkitchen.ms_mesas.client;

import java.math.BigDecimal;

public record ResumoContaAtendimentoResponse(
        Integer idAtendimento,
        BigDecimal valorFinal,
        Integer totalPedidos,
        Integer totalItens
) {
}
