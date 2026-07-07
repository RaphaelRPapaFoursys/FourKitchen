package br.com.fourkitchen.ms_pedidos.controller;

import br.com.fourkitchen.ms_pedidos.dto.response.PedidoCozinhaResponse;
import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import br.com.fourkitchen.ms_pedidos.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoControllerTest {

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private PedidoController pedidoController;

    @Test
    void listarPedidosAtivosDetalhadosPorAtendimentosDeveRetornarOk() {
        PedidoCozinhaResponse pedido = new PedidoCozinhaResponse(
                25,
                123456,
                CanaisPedido.GARCOM,
                StatusPedido.PRONTO,
                1,
                8,
                LocalDateTime.of(2026, 7, 2, 10, 30),
                List.of()
        );

        when(pedidoService.findPedidosAtivosDetalhadosPorAtendimentos(List.of(8))).thenReturn(List.of(pedido));

        ResponseEntity<List<PedidoCozinhaResponse>> response =
                pedidoController.listarPedidosAtivosDetalhadosPorAtendimentos(List.of(8));

        assertEquals(200, response.getStatusCode().value());
        assertSame(pedido, response.getBody().getFirst());
        verify(pedidoService).findPedidosAtivosDetalhadosPorAtendimentos(List.of(8));
    }
}
