package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import br.com.fourkitchen.bff_restaurante.dto.PeriodoDashboard;

import java.math.BigDecimal;
import java.util.List;

public record ProblemasCozinhaMotivoClientResponse(
        PeriodoDashboard periodo,
        long totalProblemas,
        String motivoMaisFrequente,
        List<Item> dados
) {
    public record Item(String motivo, String descricao, long quantidade, BigDecimal percentual) {
    }
}
