package br.com.fourkitchen.ms_pedidos.dto.response;

import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.PeriodoDashboard;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Pedidos agrupados pelo canal de origem.")
public record PedidosCanalResponse(
        PeriodoDashboard periodo,
        long totalPedidos,
        List<Item> dados
) {
    public record Item(
            CanaisPedido canal,
            String descricao,
            long quantidade,
            BigDecimal percentual
    ) {
    }
}
