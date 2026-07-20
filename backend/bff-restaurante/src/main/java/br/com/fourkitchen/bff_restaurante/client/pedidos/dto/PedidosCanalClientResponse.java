package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import br.com.fourkitchen.bff_restaurante.dto.PeriodoDashboard;

import java.math.BigDecimal;
import java.util.List;

public record PedidosCanalClientResponse(
        PeriodoDashboard periodo,
        long totalPedidos,
        List<Item> dados
) {
    public record Item(String canal, String descricao, long quantidade, BigDecimal percentual) {
    }
}
