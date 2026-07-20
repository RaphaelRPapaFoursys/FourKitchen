package br.com.fourkitchen.ms_pedidos.service;

import br.com.fourkitchen.ms_pedidos.enums.PeriodoDashboard;
import br.com.fourkitchen.ms_pedidos.exceptions.BaseException;
import br.com.fourkitchen.ms_pedidos.exceptions.ErrorEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class PeriodoDashboardResolver {

    private final LocalTime inicioTurno;
    private final Clock clock;

    @Autowired
    public PeriodoDashboardResolver(
            @Value("${dashboard.turno-inicio:06:00}") String inicioTurno,
            @Value("${dashboard.timezone:America/Sao_Paulo}") String timezone
    ) {
        this(LocalTime.parse(inicioTurno), Clock.system(ZoneId.of(timezone)));
    }

    PeriodoDashboardResolver(LocalTime inicioTurno, Clock clock) {
        this.inicioTurno = inicioTurno;
        this.clock = clock;
    }

    public Intervalo resolver(PeriodoDashboard periodo, LocalDate dataInicial, LocalDate dataFinal) {
        LocalDateTime fim = LocalDateTime.now(clock);
        LocalDateTime inicio = switch (periodo) {
            case ULTIMA_HORA -> fim.minusHours(1);
            case HOJE -> fim.toLocalDate().atStartOfDay();
            case TURNO_ATUAL -> inicioTurno(fim);
            case ONTEM -> fim.toLocalDate().minusDays(1).atStartOfDay();
            case ULTIMOS_7_DIAS -> fim.toLocalDate().minusDays(6).atStartOfDay();
            case ULTIMOS_30_DIAS -> fim.toLocalDate().minusDays(29).atStartOfDay();
            case PERSONALIZADO -> validarDataPersonalizada(dataInicial, dataFinal).atStartOfDay();
        };
        if (periodo == PeriodoDashboard.ONTEM) {
            fim = fim.toLocalDate().atStartOfDay();
        } else if (periodo == PeriodoDashboard.PERSONALIZADO) {
            fim = dataFinal.plusDays(1).atStartOfDay();
        }
        return new Intervalo(inicio, fim);
    }

    private LocalDate validarDataPersonalizada(LocalDate dataInicial, LocalDate dataFinal) {
        if (dataInicial == null || dataFinal == null || dataInicial.isAfter(dataFinal)
                || dataInicial.plusYears(1).isBefore(dataFinal)) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
        return dataInicial;
    }

    private LocalDateTime inicioTurno(LocalDateTime agora) {
        LocalDate data = agora.toLocalTime().isBefore(inicioTurno)
                ? agora.toLocalDate().minusDays(1)
                : agora.toLocalDate();
        return LocalDateTime.of(data, inicioTurno);
    }

    public record Intervalo(LocalDateTime inicio, LocalDateTime fim) {
    }
}
