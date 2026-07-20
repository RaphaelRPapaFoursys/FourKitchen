package br.com.fourkitchen.ms_pedidos.service;

import br.com.fourkitchen.ms_pedidos.dto.response.PedidosCanalResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.ProblemasCozinhaMotivoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.RankingProdutosResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.VolumePedidosHorarioResponse;
import br.com.fourkitchen.ms_pedidos.enums.PeriodoDashboard;
import br.com.fourkitchen.ms_pedidos.repository.PedidoRepository;
import br.com.fourkitchen.ms_pedidos.repository.ProblemaCozinhaRepository;
import br.com.fourkitchen.ms_pedidos.repository.ProdutoPedidoRepository;
import br.com.fourkitchen.ms_pedidos.repository.projection.CanalQuantidadeProjection;
import br.com.fourkitchen.ms_pedidos.repository.projection.MotivoQuantidadeProjection;
import br.com.fourkitchen.ms_pedidos.repository.projection.ProdutoRankingProjection;
import br.com.fourkitchen.ms_pedidos.repository.projection.VolumeHorarioProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoDashboardServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ProblemaCozinhaRepository problemaRepository;
    @Mock
    private ProdutoPedidoRepository produtoPedidoRepository;

    private PedidoDashboardService service;

    @BeforeEach
    void setup() {
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        Clock clock = Clock.fixed(Instant.parse("2026-07-20T16:30:00Z"), zone);
        service = new PedidoDashboardService(
                pedidoRepository,
                problemaRepository,
                produtoPedidoRepository,
                new PeriodoDashboardResolver(LocalTime.of(6, 0), clock)
        );
    }

    @Test
    void devePreencherHorasVaziasEUsarPrimeiroPico() {
        VolumeHorarioProjection onze = volume(LocalDateTime.of(2026, 7, 20, 11, 0), 4L);
        VolumeHorarioProjection treze = volume(LocalDateTime.of(2026, 7, 20, 13, 0), 4L);
        when(pedidoRepository.contarPorHorario(
                LocalDateTime.of(2026, 7, 20, 0, 0),
                LocalDateTime.of(2026, 7, 20, 13, 30), null, null, null
        )).thenReturn(List.of(onze, treze));

        VolumePedidosHorarioResponse response = service.buscarVolumePorHorario(filtro(PeriodoDashboard.HOJE));

        assertEquals(14, response.dados().size());
        assertEquals(0, response.dados().get(12).quantidade());
        assertEquals(8, response.totalPedidos());
        assertEquals("11:00", response.horarioPico());
        assertEquals(4, response.quantidadeNoPico());
    }

    @Test
    void deveOrdenarProblemasECalcularPercentuais() {
        MotivoQuantidadeProjection indisponivel = motivo("INDISPONIVEL", 3L);
        MotivoQuantidadeProjection erro = motivo("ERRO", 1L);
        when(problemaRepository.contarPorMotivo(
                LocalDateTime.of(2026, 7, 20, 0, 0),
                LocalDateTime.of(2026, 7, 20, 13, 30), null, null, null
        )).thenReturn(List.of(indisponivel, erro));

        ProblemasCozinhaMotivoResponse response = service.buscarProblemasPorMotivo(filtro(PeriodoDashboard.HOJE));

        assertEquals(4, response.totalProblemas());
        assertEquals("INDISPONIVEL", response.motivoMaisFrequente().name());
        assertEquals("75.00", response.dados().getFirst().percentual().toPlainString());
        assertEquals("25.00", response.dados().getLast().percentual().toPlainString());
    }

    @Test
    void deveRetornarTodosOsCanaisMesmoSemPedidos() {
        CanalQuantidadeProjection mesa = canal("MESA", 2L);
        when(pedidoRepository.contarPorCanal(
                LocalDateTime.of(2026, 7, 20, 12, 30),
                LocalDateTime.of(2026, 7, 20, 13, 30), null, null, null
        )).thenReturn(List.of(mesa));

        PedidosCanalResponse response = service.buscarPedidosPorCanal(filtro(PeriodoDashboard.ULTIMA_HORA));

        assertEquals(3, response.dados().size());
        assertEquals(2, response.totalPedidos());
        assertEquals(0, response.dados().getFirst().quantidade());
        assertEquals("100.00", response.dados().get(1).percentual().toPlainString());
    }

    @Test
    void deveRetornarPicoNuloSemDados() {
        when(pedidoRepository.contarPorHorario(
                LocalDateTime.of(2026, 7, 20, 12, 30),
                LocalDateTime.of(2026, 7, 20, 13, 30), null, null, null
        )).thenReturn(List.of());

        VolumePedidosHorarioResponse response = service.buscarVolumePorHorario(filtro(PeriodoDashboard.ULTIMA_HORA));

        assertEquals(0, response.totalPedidos());
        assertNull(response.horarioPico());
    }

    @Test
    void deveMapearRankingDeProdutosNoPeriodo() {
        ProdutoRankingProjection produto = mock(ProdutoRankingProjection.class);
        when(produto.getIdProduto()).thenReturn(8);
        when(produto.getNomeProduto()).thenReturn("Risoto");
        when(produto.getQuantidade()).thenReturn(12L);
        when(produtoPedidoRepository.buscarRankingProdutos(
                LocalDateTime.of(2026, 7, 14, 0, 0),
                LocalDateTime.of(2026, 7, 20, 13, 30)
        )).thenReturn(List.of(produto));

        RankingProdutosResponse response = service.buscarRankingProdutos(PeriodoDashboard.ULTIMOS_7_DIAS);

        assertEquals(PeriodoDashboard.ULTIMOS_7_DIAS, response.periodo());
        assertEquals(8, response.dados().getFirst().idProduto());
        assertEquals("Risoto", response.dados().getFirst().nomeProduto());
        assertEquals(12, response.dados().getFirst().quantidade());
    }

    private VolumeHorarioProjection volume(LocalDateTime horario, long quantidade) {
        VolumeHorarioProjection projection = mock(VolumeHorarioProjection.class);
        when(projection.getHorario()).thenReturn(horario);
        when(projection.getQuantidade()).thenReturn(quantidade);
        return projection;
    }

    private MotivoQuantidadeProjection motivo(String motivo, long quantidade) {
        MotivoQuantidadeProjection projection = mock(MotivoQuantidadeProjection.class);
        when(projection.getMotivo()).thenReturn(motivo);
        when(projection.getQuantidade()).thenReturn(quantidade);
        return projection;
    }

    private CanalQuantidadeProjection canal(String canal, long quantidade) {
        CanalQuantidadeProjection projection = mock(CanalQuantidadeProjection.class);
        when(projection.getCanal()).thenReturn(canal);
        when(projection.getQuantidade()).thenReturn(quantidade);
        return projection;
    }

    private FiltroDashboard filtro(PeriodoDashboard periodo) {
        return new FiltroDashboard(periodo, null, null, null, null, null);
    }
}
