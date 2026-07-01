package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoMesaRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.ItemPedidoMesaRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoMesaResponse;
import br.com.fourkitchen.bff_restaurante.service.MesaPedidoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MesaPedidoControllerTest {

    @Mock
    private MesaPedidoService mesaPedidoService;

    @InjectMocks
    private MesaPedidoController mesaPedidoController;

    @Test
    void criarPedidoDeveRetornarCreated() {
        CriarPedidoMesaRequest request = new CriarPedidoMesaRequest(
                1,
                123456,
                List.of(new ItemPedidoMesaRequest(10, 2, new BigDecimal("29.90"), null))
        );
        PedidoMesaResponse pedidoResponse = new PedidoMesaResponse(
                25,
                100025,
                "MESA",
                "ENVIADO_COZINHA",
                1,
                8
        );

        when(mesaPedidoService.criarPedido(request)).thenReturn(pedidoResponse);

        ResponseEntity<PedidoMesaResponse> response = mesaPedidoController.criarPedido(request);

        assertEquals(201, response.getStatusCode().value());
        assertSame(pedidoResponse, response.getBody());
        verify(mesaPedidoService).criarPedido(request);
    }
}
