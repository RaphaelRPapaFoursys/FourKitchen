package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.MesaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.service.GarcomMesaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GarcomMesaControllerTest {

    @Mock
    private GarcomMesaService garcomMesaService;

    @InjectMocks
    private GarcomMesaController garcomMesaController;

    @Test
    void listarMesasDeveRetornarOk() {
        Authentication authentication = mock(Authentication.class);
        MesaGarcomResponse mesaResponse = new MesaGarcomResponse(
                1,
                10,
                "OCUPADA",
                8,
                123456,
                7,
                LocalDateTime.of(2026, 7, 2, 10, 0),
                List.of(),
                List.of(),
                false
        );

        when(garcomMesaService.listarMesas(authentication)).thenReturn(List.of(mesaResponse));

        ResponseEntity<List<MesaGarcomResponse>> response = garcomMesaController.listarMesas(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertSame(mesaResponse, response.getBody().getFirst());
        verify(garcomMesaService).listarMesas(authentication);
    }
}
