package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidosCanalClientResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.RankingProdutosClientResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.VolumePedidosHorarioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.FiltroDashboard;
import br.com.fourkitchen.bff_restaurante.dto.PeriodoDashboard;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidosCanalResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.RankingProdutosResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.VolumePedidosHorarioResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorDashboardServiceTest {

    @Mock
    private PedidoClient pedidoClient;
    @InjectMocks
    private GestorDashboardService service;

    @Test
    void deveMapearVolumePorHorario() {
        when(pedidoClient.buscarPedidosPorHorario("HOJE", null, null, null, null, null)).thenReturn(new VolumePedidosHorarioClientResponse(
                PeriodoDashboard.HOJE, 3, "12:00", 3,
                List.of(new VolumePedidosHorarioClientResponse.Item("12:00", 3))
        ));

        VolumePedidosHorarioResponse response = service.buscarPedidosPorHorario(filtro(PeriodoDashboard.HOJE));

        assertEquals(3, response.totalPedidos());
        assertEquals("12:00", response.dados().getFirst().horario());
    }

    @Test
    void deveManterCanaisTipadosNoContratoExterno() {
        when(pedidoClient.buscarPedidosPorCanal("ULTIMA_HORA", null, null, null, null, null)).thenReturn(new PedidosCanalClientResponse(
                PeriodoDashboard.ULTIMA_HORA, 2,
                List.of(new PedidosCanalClientResponse.Item("MESA", "Tablet da mesa", 2, new BigDecimal("100.00")))
        ));

        PedidosCanalResponse response = service.buscarPedidosPorCanal(filtro(PeriodoDashboard.ULTIMA_HORA));

        assertEquals("MESA", response.dados().getFirst().canal());
        assertEquals(new BigDecimal("100.00"), response.dados().getFirst().percentual());
    }

    @Test
    void deveConverterFalhaFeignEmServicoIndisponivel() {
        when(pedidoClient.buscarPedidosPorHorario("HOJE", null, null, null, null, null)).thenThrow(mock(FeignException.class));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> service.buscarPedidosPorHorario(filtro(PeriodoDashboard.HOJE))
        );

        assertEquals(ErrorEnum.DADOS_DASHBOARD_INDISPONIVEIS, exception.getErrorEnum());
        assertEquals(503, exception.getErrorEnum().getHttpStatus().value());
    }

    @Test
    void deveMapearRankingDeProdutos() {
        when(pedidoClient.buscarRankingProdutos("ULTIMOS_7_DIAS")).thenReturn(new RankingProdutosClientResponse(
                PeriodoDashboard.ULTIMOS_7_DIAS,
                List.of(new RankingProdutosClientResponse.Item(9, "Hambúrguer", 18))
        ));

        RankingProdutosResponse response = service.buscarRankingProdutos(PeriodoDashboard.ULTIMOS_7_DIAS);

        assertEquals("Hambúrguer", response.dados().getFirst().nomeProduto());
        assertEquals(18, response.dados().getFirst().quantidade());
    }

    private FiltroDashboard filtro(PeriodoDashboard periodo) {
        return new FiltroDashboard(periodo, null, null, null, null, null);
    }
}
