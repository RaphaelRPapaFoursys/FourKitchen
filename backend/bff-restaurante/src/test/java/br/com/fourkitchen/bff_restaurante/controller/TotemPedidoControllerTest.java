package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoTotemRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.ItemPedidoTotemRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoTotemResponse;
import br.com.fourkitchen.bff_restaurante.service.TotemPedidoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TotemPedidoControllerTest {

    @Mock
    private TotemPedidoService totemPedidoService;

    @InjectMocks
    private TotemPedidoController totemPedidoController;

    @Test
    void criarPedidoDeveRetornarCreated() {
        CriarPedidoTotemRequest request = new CriarPedidoTotemRequest(
                List.of(new ItemPedidoTotemRequest(10, 2, null))
        );
        PedidoTotemResponse pedidoResponse = new PedidoTotemResponse(
                25,
                100025,
                "TOTEM",
                "ENVIADO_COZINHA"
        );

        when(totemPedidoService.criarPedido(request)).thenReturn(pedidoResponse);

        ResponseEntity<PedidoTotemResponse> response = totemPedidoController.criarPedido(request);

        assertEquals(201, response.getStatusCode().value());
        assertSame(pedidoResponse, response.getBody());
        verify(totemPedidoService).criarPedido(request);
    }
}
