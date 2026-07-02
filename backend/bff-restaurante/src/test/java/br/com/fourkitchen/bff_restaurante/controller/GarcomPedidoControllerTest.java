package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.ItemPedidoGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoGarcomResponse;
import br.com.fourkitchen.bff_restaurante.service.GarcomPedidoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GarcomPedidoControllerTest {

    @Mock
    private GarcomPedidoService garcomPedidoService;

    @InjectMocks
    private GarcomPedidoController garcomPedidoController;

    @Test
    void criarPedidoDeveRetornarCreated() {
        Authentication authentication = mock(Authentication.class);
        CriarPedidoGarcomRequest request = new CriarPedidoGarcomRequest(
                1,
                List.of(new ItemPedidoGarcomRequest(10, 2, new BigDecimal("29.90"), null))
        );
        PedidoGarcomResponse pedidoResponse = new PedidoGarcomResponse(
                25,
                100025,
                "GARCOM",
                "ENVIADO_COZINHA",
                1,
                7,
                8
        );

        when(garcomPedidoService.criarPedido(request, authentication)).thenReturn(pedidoResponse);

        ResponseEntity<PedidoGarcomResponse> response = garcomPedidoController.criarPedido(request, authentication);

        assertEquals(201, response.getStatusCode().value());
        assertSame(pedidoResponse, response.getBody());
        verify(garcomPedidoService).criarPedido(request, authentication);
    }
}
