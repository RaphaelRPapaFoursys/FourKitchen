package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaCardapioResponse;
import br.com.fourkitchen.bff_restaurante.service.CardapioService;
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
class CardapioControllerTest {

    @Mock
    private CardapioService cardapioService;

    @InjectMocks
    private CardapioController cardapioController;

    @Test
    void buscarCardapioMesaDeveRetornarCardapio() {
        List<CategoriaCardapioResponse> cardapio =
                List.of(new CategoriaCardapioResponse(1, "Lanches", "Sanduiches", null, List.of()));

        when(cardapioService.buscarCardapio()).thenReturn(cardapio);

        ResponseEntity<List<CategoriaCardapioResponse>> response = cardapioController.buscarCardapioMesa();

        assertEquals(200, response.getStatusCode().value());
        assertSame(cardapio, response.getBody());
        verify(cardapioService).buscarCardapio();
    }

    @Test
    void buscarCardapioTotemDeveRetornarCardapio() {
        List<CategoriaCardapioResponse> cardapio =
                List.of(new CategoriaCardapioResponse(1, "Lanches", "Sanduiches", null, List.of()));

        when(cardapioService.buscarCardapio()).thenReturn(cardapio);

        ResponseEntity<List<CategoriaCardapioResponse>> response = cardapioController.buscarCardapioTotem();

        assertEquals(200, response.getStatusCode().value());
        assertSame(cardapio, response.getBody());
        verify(cardapioService).buscarCardapio();
    }

    @Test
    void buscarCardapioGarcomDeveRetornarCardapio() {
        List<CategoriaCardapioResponse> cardapio =
                List.of(new CategoriaCardapioResponse(1, "Lanches", "Sanduiches", null, List.of()));

        when(cardapioService.buscarCardapio()).thenReturn(cardapio);

        ResponseEntity<List<CategoriaCardapioResponse>> response = cardapioController.buscarCardapioGarcom();

        assertEquals(200, response.getStatusCode().value());
        assertSame(cardapio, response.getBody());
        verify(cardapioService).buscarCardapio();
    }
}
