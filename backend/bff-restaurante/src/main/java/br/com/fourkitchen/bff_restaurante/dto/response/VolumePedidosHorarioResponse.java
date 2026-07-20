package br.com.fourkitchen.bff_restaurante.dto.response;

import br.com.fourkitchen.bff_restaurante.dto.PeriodoDashboard;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Volume de pedidos por hora no período selecionado.")
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
