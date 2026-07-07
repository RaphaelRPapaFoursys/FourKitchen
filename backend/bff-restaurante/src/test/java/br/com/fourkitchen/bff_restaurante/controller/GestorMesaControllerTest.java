package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.AtribuirGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.GarcomResumoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGestorResponse;
import br.com.fourkitchen.bff_restaurante.service.GestorMesaService;
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
class GestorMesaControllerTest {

    private static final String AUTHORIZATION = "Bearer token";

    @Mock
    private GestorMesaService gestorMesaService;

    @InjectMocks
    private GestorMesaController gestorMesaController;

    @Test
    void listarMesasDeveRetornarOk() {
        MesaGestorResponse mesa = criarMesa();

        when(gestorMesaService.listarMesas(AUTHORIZATION)).thenReturn(List.of(mesa));

        ResponseEntity<List<MesaGestorResponse>> response = gestorMesaController.listarMesas(AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(mesa, response.getBody().getFirst());
        verify(gestorMesaService).listarMesas(AUTHORIZATION);
    }

    @Test
    void listarGarconsDeveRetornarOk() {
        GarcomResumoResponse garcom = new GarcomResumoResponse(
                7,
                "Amanda Souza",
                "amanda@fourkitchen.com"
        );

        when(gestorMesaService.listarGarcons(AUTHORIZATION)).thenReturn(List.of(garcom));

        ResponseEntity<List<GarcomResumoResponse>> response = gestorMesaController.listarGarcons(AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(garcom, response.getBody().getFirst());
        verify(gestorMesaService).listarGarcons(AUTHORIZATION);
    }

    @Test
    void abrirMesaDeveRetornarOk() {
        MesaGestorResponse mesa = criarMesa();

        when(gestorMesaService.abrirMesa(1, AUTHORIZATION)).thenReturn(mesa);

        ResponseEntity<MesaGestorResponse> response = gestorMesaController.abrirMesa(1, AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(mesa, response.getBody());
        verify(gestorMesaService).abrirMesa(1, AUTHORIZATION);
    }

    @Test
    void fecharMesaDeveRetornarOk() {
        MesaGestorResponse mesa = criarMesa();

        when(gestorMesaService.fecharMesa(1, AUTHORIZATION)).thenReturn(mesa);

        ResponseEntity<MesaGestorResponse> response = gestorMesaController.fecharMesa(1, AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(mesa, response.getBody());
        verify(gestorMesaService).fecharMesa(1, AUTHORIZATION);
    }

    @Test
    void atribuirGarcomDeveRetornarOk() {
        AtribuirGarcomRequest request = new AtribuirGarcomRequest(7);
        MesaGestorResponse mesa = criarMesa();

        when(gestorMesaService.atribuirGarcom(1, request, AUTHORIZATION)).thenReturn(mesa);

        ResponseEntity<MesaGestorResponse> response = gestorMesaController.atribuirGarcom(
                1,
                request,
                AUTHORIZATION
        );

        assertEquals(200, response.getStatusCode().value());
        assertSame(mesa, response.getBody());
        verify(gestorMesaService).atribuirGarcom(1, request, AUTHORIZATION);
    }

    private MesaGestorResponse criarMesa() {
        return new MesaGestorResponse(
                1,
                10,
                "OCUPADA",
                7,
                "Amanda Souza",
                123456,
                LocalDateTime.of(2026, 7, 2, 10, 0),
                null,
                List.of()
        );
    }
}
