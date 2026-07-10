package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.PedidoFilaCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoStatusCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.service.CozinhaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CozinhaControllerTest {

    @Mock
    private CozinhaService cozinhaService;

    @InjectMocks
    private CozinhaController cozinhaController;

    @Test
    void listarFilaDeveRetornarOk() {
        PedidoFilaCozinhaResponse pedido = new PedidoFilaCozinhaResponse(
                25,
                123456,
                "MESA",
                "ENVIADO_COZINHA",
                1,
                8,
                LocalDateTime.of(2026, 7, 2, 10, 30),
                null,
                null,
                List.of()
        );

        when(cozinhaService.listarFila()).thenReturn(List.of(pedido));

        ResponseEntity<List<PedidoFilaCozinhaResponse>> response = cozinhaController.listarFila();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(pedido), response.getBody());
        verify(cozinhaService).listarFila();
    }

    @Test
    void iniciarPreparoDeveRetornarOk() {
        PedidoStatusCozinhaResponse pedido = new PedidoStatusCozinhaResponse(
                25,
                123456,
                "GARCOM",
                "EM_PREPARO",
                1,
                8
        );

        when(cozinhaService.iniciarPreparo(25)).thenReturn(pedido);

        ResponseEntity<PedidoStatusCozinhaResponse> response = cozinhaController.iniciarPreparo(25);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(pedido, response.getBody());
        verify(cozinhaService).iniciarPreparo(25);
    }

    @Test
    void finalizarPreparoDeveRetornarOk() {
        PedidoStatusCozinhaResponse pedido = new PedidoStatusCozinhaResponse(
                25,
                123456,
                "GARCOM",
                "PRONTO",
                1,
                8
        );

        when(cozinhaService.finalizarPreparo(25)).thenReturn(pedido);

        ResponseEntity<PedidoStatusCozinhaResponse> response = cozinhaController.finalizarPreparo(25);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(pedido, response.getBody());
        verify(cozinhaService).finalizarPreparo(25);
    }
}
