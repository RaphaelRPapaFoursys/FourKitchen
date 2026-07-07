package br.com.fourkitchen.ms_pedidos.dto.response;

import java.math.BigDecimal;

public record ResumoContaAtendimentoResponse(
        Integer idAtendimento,
        BigDecimal valorFinal,
        Integer totalPedidos,
        Integer totalItens
) {
}
