package br.com.fourkitchen.ms_pedidos.service;

import br.com.fourkitchen.ms_pedidos.enums.PeriodoDashboard;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeriodoDashboardResolverTest {

    @Test
    void turnoAntesDasSeisDeveComecarNoDiaAnterior() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-20T08:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        PeriodoDashboardResolver resolver = new PeriodoDashboardResolver(LocalTime.of(6, 0), clock);

        PeriodoDashboardResolver.Intervalo intervalo = resolver.resolver(PeriodoDashboard.TURNO_ATUAL, null, null);

        assertEquals(LocalDateTime.of(2026, 7, 19, 6, 0), intervalo.inicio());
        assertEquals(LocalDateTime.of(2026, 7, 20, 5, 0), intervalo.fim());
    }
}
