package br.com.fourkitchen.ms_pedidos.service;

import br.com.fourkitchen.ms_pedidos.dto.response.ResumoTotemResponse;
import br.com.fourkitchen.ms_pedidos.repository.PedidoRepository;
import br.com.fourkitchen.ms_pedidos.repository.projection.ResumoTotemProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoTotemResumoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ResumoTotemProjection projection;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void deveMapearResumoOperacionalDosTotens() {
        LocalDateTime ultimaAtividade = LocalDateTime.of(2026, 7, 22, 14, 30);
        when(projection.getIdUsuario()).thenReturn(9);
        when(projection.getPedidosHoje()).thenReturn(7L);
        when(projection.getValorHoje()).thenReturn(new BigDecimal("245.50"));
        when(projection.getUltimaAtividade()).thenReturn(ultimaAtividade);
        when(projection.getProblemasAbertos()).thenReturn(1L);
        when(pedidoRepository.resumirTotens(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(projection));

        List<ResumoTotemResponse> resultado = pedidoService.resumirTotens();

        assertEquals(1, resultado.size());
        assertEquals(9, resultado.getFirst().idUsuario());
        assertEquals(7L, resultado.getFirst().pedidosHoje());
        assertEquals(new BigDecimal("245.50"), resultado.getFirst().valorHoje());
        assertEquals(ultimaAtividade, resultado.getFirst().ultimaAtividade());
        assertEquals(1L, resultado.getFirst().problemasAbertos());
    }
}
