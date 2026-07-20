package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import br.com.fourkitchen.bff_restaurante.dto.PeriodoDashboard;

import java.util.List;

public record VolumePedidosHorarioClientResponse(
        PeriodoDashboard periodo,
        long totalPedidos,
        String horarioPico,
        long quantidadeNoPico,
        List<Item> dados
) {
    public record Item(String horario, long quantidade) {
    }
}
