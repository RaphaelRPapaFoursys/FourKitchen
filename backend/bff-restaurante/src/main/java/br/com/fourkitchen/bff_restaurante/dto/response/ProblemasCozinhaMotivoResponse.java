package br.com.fourkitchen.bff_restaurante.dto.response;

import br.com.fourkitchen.bff_restaurante.dto.PeriodoDashboard;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Problemas registrados pela cozinha agrupados por motivo.")
public record ProblemasCozinhaMotivoResponse(
        PeriodoDashboard periodo,
        long totalProblemas,
        String motivoMaisFrequente,
        List<Item> dados
) {
    public record Item(String motivo, String descricao, long quantidade, BigDecimal percentual) {
    }
}
