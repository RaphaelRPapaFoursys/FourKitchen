package br.com.fourkitchen.bff_restaurante.dto;

import java.time.LocalDate;

public record FiltroDashboard(
        PeriodoDashboard periodo,
        LocalDate dataInicial,
        LocalDate dataFinal,
        String canal,
        Integer idMesa,
        String status
) {
}
