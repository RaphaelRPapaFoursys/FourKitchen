package br.com.fourkitchen.ms_pedidos.dto.response;

import br.com.fourkitchen.ms_pedidos.enums.PeriodoDashboard;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Volume de pedidos agrupado por hora.")
public record VolumePedidosHorarioResponse(
        PeriodoDashboard periodo,
        long totalPedidos,
        String horarioPico,
        long quantidadeNoPico,
        List<Item> dados
) {
    public record Item(String horario, long quantidade) {
    }
}
