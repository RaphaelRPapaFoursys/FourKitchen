package br.com.fourkitchen.ms_pedidos.dto.response;

import br.com.fourkitchen.ms_pedidos.enums.PeriodoDashboard;
import br.com.fourkitchen.ms_pedidos.enums.StatusProdutoPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Problemas registrados pela cozinha agrupados por motivo.")
public record ProblemasCozinhaMotivoResponse(
        PeriodoDashboard periodo,
        long totalProblemas,
        StatusProdutoPedido motivoMaisFrequente,
        List<Item> dados
) {
    public record Item(
            StatusProdutoPedido motivo,
            String descricao,
            long quantidade,
            BigDecimal percentual
    ) {
    }
}
