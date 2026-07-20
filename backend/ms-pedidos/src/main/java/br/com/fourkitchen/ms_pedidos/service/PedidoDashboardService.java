package br.com.fourkitchen.ms_pedidos.service;

import br.com.fourkitchen.ms_pedidos.dto.response.PedidosCanalResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.ProblemasCozinhaMotivoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.RankingProdutosResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.VolumePedidosHorarioResponse;
import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.PeriodoDashboard;
import br.com.fourkitchen.ms_pedidos.enums.StatusProdutoPedido;
import br.com.fourkitchen.ms_pedidos.repository.PedidoRepository;
import br.com.fourkitchen.ms_pedidos.repository.ProblemaCozinhaRepository;
import br.com.fourkitchen.ms_pedidos.repository.ProdutoPedidoRepository;
import br.com.fourkitchen.ms_pedidos.repository.projection.CanalQuantidadeProjection;
import br.com.fourkitchen.ms_pedidos.repository.projection.MotivoQuantidadeProjection;
import br.com.fourkitchen.ms_pedidos.repository.projection.VolumeHorarioProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PedidoDashboardService {

    private static final DateTimeFormatter HORARIO_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final PedidoRepository pedidoRepository;
    private final ProblemaCozinhaRepository problemaCozinhaRepository;
    private final ProdutoPedidoRepository produtoPedidoRepository;
    private final PeriodoDashboardResolver periodoResolver;

    public VolumePedidosHorarioResponse buscarVolumePorHorario(FiltroDashboard filtro) {
        PeriodoDashboardResolver.Intervalo intervalo = periodoResolver.resolver(filtro.periodo(), filtro.dataInicial(), filtro.dataFinal());
        Map<LocalDateTime, Long> quantidades = new HashMap<>();
        for (VolumeHorarioProjection item : pedidoRepository.contarPorHorario(
                intervalo.inicio(), intervalo.fim(), valor(filtro.canal()), filtro.idMesa(), valor(filtro.status()))) {
            quantidades.put(item.getHorario().truncatedTo(ChronoUnit.HOURS), item.getQuantidade());
        }

        LocalDateTime primeiroHorario = intervalo.inicio().truncatedTo(ChronoUnit.HOURS);
        LocalDateTime ultimoHorario = intervalo.fim().minusNanos(1).truncatedTo(ChronoUnit.HOURS);
        List<VolumePedidosHorarioResponse.Item> dados = Stream
                .iterate(primeiroHorario, horario -> !horario.isAfter(ultimoHorario), horario -> horario.plusHours(1))
                .map(horario -> new VolumePedidosHorarioResponse.Item(
                        HORARIO_FORMATTER.format(horario),
                        quantidades.getOrDefault(horario, 0L)
                ))
                .toList();

        long total = dados.stream().mapToLong(VolumePedidosHorarioResponse.Item::quantidade).sum();
        VolumePedidosHorarioResponse.Item pico = dados.stream()
                .reduce((primeiro, atual) -> atual.quantidade() > primeiro.quantidade() ? atual : primeiro)
                .orElse(null);

        return new VolumePedidosHorarioResponse(
                filtro.periodo(),
                total,
                total == 0 || pico == null ? null : pico.horario(),
                total == 0 || pico == null ? 0 : pico.quantidade(),
                dados
        );
    }

    public ProblemasCozinhaMotivoResponse buscarProblemasPorMotivo(FiltroDashboard filtro) {
        PeriodoDashboardResolver.Intervalo intervalo = periodoResolver.resolver(filtro.periodo(), filtro.dataInicial(), filtro.dataFinal());
        List<MotivoQuantidadeProjection> agregados = problemaCozinhaRepository
                .contarPorMotivo(intervalo.inicio(), intervalo.fim(), valor(filtro.canal()), filtro.idMesa(), valor(filtro.status()));
        long total = agregados.stream().mapToLong(MotivoQuantidadeProjection::getQuantidade).sum();
        List<ProblemasCozinhaMotivoResponse.Item> dados = agregados.stream()
                .map(item -> {
                    StatusProdutoPedido motivo = StatusProdutoPedido.valueOf(item.getMotivo());
                    return new ProblemasCozinhaMotivoResponse.Item(
                            motivo,
                            descricaoMotivo(motivo),
                            item.getQuantidade(),
                            percentual(item.getQuantidade(), total)
                    );
                })
                .toList();

        return new ProblemasCozinhaMotivoResponse(
                filtro.periodo(),
                total,
                dados.isEmpty() ? null : dados.getFirst().motivo(),
                dados
        );
    }

    public PedidosCanalResponse buscarPedidosPorCanal(FiltroDashboard filtro) {
        PeriodoDashboardResolver.Intervalo intervalo = periodoResolver.resolver(filtro.periodo(), filtro.dataInicial(), filtro.dataFinal());
        Map<CanaisPedido, Long> quantidades = new EnumMap<>(CanaisPedido.class);
        for (CanalQuantidadeProjection item : pedidoRepository.contarPorCanal(
                intervalo.inicio(), intervalo.fim(), valor(filtro.canal()), filtro.idMesa(), valor(filtro.status()))) {
            quantidades.put(CanaisPedido.valueOf(item.getCanal()), item.getQuantidade());
        }
        long total = quantidades.values().stream().mapToLong(Long::longValue).sum();
        List<PedidosCanalResponse.Item> dados = Arrays.stream(CanaisPedido.values())
                .map(canal -> {
                    long quantidade = quantidades.getOrDefault(canal, 0L);
                    return new PedidosCanalResponse.Item(
                            canal,
                            descricaoCanal(canal),
                            quantidade,
                            percentual(quantidade, total)
                    );
                })
                .toList();
        return new PedidosCanalResponse(filtro.periodo(), total, dados);
    }

    public RankingProdutosResponse buscarRankingProdutos(PeriodoDashboard periodo) {
        PeriodoDashboardResolver.Intervalo intervalo = periodoResolver.resolver(periodo, null, null);
        List<RankingProdutosResponse.Item> dados = produtoPedidoRepository
                .buscarRankingProdutos(intervalo.inicio(), intervalo.fim())
                .stream()
                .map(item -> new RankingProdutosResponse.Item(
                        item.getIdProduto(), item.getNomeProduto(), item.getQuantidade()))
                .toList();
        return new RankingProdutosResponse(periodo, dados);
    }

    private String valor(Enum<?> valor) {
        return valor == null ? null : valor.name();
    }

    private BigDecimal percentual(long quantidade, long total) {
        if (total == 0) return BigDecimal.ZERO.setScale(2);
        return BigDecimal.valueOf(quantidade)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private String descricaoMotivo(StatusProdutoPedido motivo) {
        return switch (motivo) {
            case FALTA_PRODUTO -> "Falta de produto";
            case ERRO -> "Erro no preparo";
            case INDISPONIVEL -> "Produto indisponível";
            default -> motivo.name();
        };
    }

    private String descricaoCanal(CanaisPedido canal) {
        return switch (canal) {
            case TOTEM -> "Totem";
            case MESA -> "Tablet da mesa";
            case GARCOM -> "Garçom";
        };
    }
}
