package br.com.fourkitchen.ms_pedidos.service;

import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.PeriodoDashboard;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;

import java.time.LocalDate;

public record FiltroDashboard(
        PeriodoDashboard periodo,
        LocalDate dataInicial,
        LocalDate dataFinal,
        CanaisPedido canal,
        Integer idMesa,
        StatusPedido status
) {
}
