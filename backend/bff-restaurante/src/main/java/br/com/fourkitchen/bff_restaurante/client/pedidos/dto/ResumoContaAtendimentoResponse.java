package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import java.math.BigDecimal;

public record ResumoContaAtendimentoResponse(
        Integer idAtendimento,
        BigDecimal valorFinal,
        Integer totalPedidos,
        Integer totalItens
) {
}
