package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.FiltroDashboard;
import br.com.fourkitchen.bff_restaurante.dto.PeriodoDashboard;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidosCanalResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ProblemasCozinhaMotivoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.RankingProdutosResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.VolumePedidosHorarioResponse;
import br.com.fourkitchen.bff_restaurante.service.GestorDashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorDashboardControllerTest {

    @Mock
    private GestorDashboardService service;
    @InjectMocks
    private GestorDashboardController controller;

    @Test
    void deveExporOsTresGraficos() {
        VolumePedidosHorarioResponse volume = new VolumePedidosHorarioResponse(PeriodoDashboard.HOJE, 0, null, 0, List.of());
        ProblemasCozinhaMotivoResponse problemas = new ProblemasCozinhaMotivoResponse(PeriodoDashboard.HOJE, 0, null, List.of());
        PedidosCanalResponse canais = new PedidosCanalResponse(PeriodoDashboard.HOJE, 0, List.of());
        FiltroDashboard filtro = new FiltroDashboard(PeriodoDashboard.HOJE, null, null, null, null, null);
        when(service.buscarPedidosPorHorario(filtro)).thenReturn(volume);
        when(service.buscarProblemasPorMotivo(filtro)).thenReturn(problemas);
        when(service.buscarPedidosPorCanal(filtro)).thenReturn(canais);

        assertEquals(volume, controller.pedidosPorHorario(PeriodoDashboard.HOJE, null, null, null, null, null).getBody());
        assertEquals(problemas, controller.problemasPorMotivo(PeriodoDashboard.HOJE, null, null, null, null, null).getBody());
        assertEquals(canais, controller.pedidosPorCanal(PeriodoDashboard.HOJE, null, null, null, null, null).getBody());
        verify(service).buscarPedidosPorHorario(filtro);
        verify(service).buscarProblemasPorMotivo(filtro);
        verify(service).buscarPedidosPorCanal(filtro);
    }

    @Test
    void deveExporRankingDeProdutos() {
        RankingProdutosResponse ranking = new RankingProdutosResponse(PeriodoDashboard.HOJE, List.of());
        when(service.buscarRankingProdutos(PeriodoDashboard.HOJE)).thenReturn(ranking);

        assertEquals(ranking, controller.rankingProdutos(PeriodoDashboard.HOJE).getBody());
        verify(service).buscarRankingProdutos(PeriodoDashboard.HOJE);
    }
}
