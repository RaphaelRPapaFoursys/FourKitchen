package br.com.fourkitchen.bff_restaurante.dto.response;

import br.com.fourkitchen.bff_restaurante.dto.PeriodoDashboard;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Pedidos agrupados pelo canal de origem.")
public record PedidosCanalResponse(
        PeriodoDashboard periodo,
        long totalPedidos,
        List<Item> dados
) {
    public record Item(String canal, String descricao, long quantidade, BigDecimal percentual) {
    }
}
