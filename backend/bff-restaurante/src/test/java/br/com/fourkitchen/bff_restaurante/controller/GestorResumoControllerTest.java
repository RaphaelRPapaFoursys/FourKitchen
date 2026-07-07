package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.ResumoOperacaoResponse;
import br.com.fourkitchen.bff_restaurante.service.GestorResumoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorResumoControllerTest {

    @Mock
    private GestorResumoService gestorResumoService;

    @InjectMocks
    private GestorResumoController gestorResumoController;

    @Test
    void buscarResumoDeveRetornarOk() {
        ResumoOperacaoResponse resumo = new ResumoOperacaoResponse(5L, 3L, 8L, 2L, 4L);

        when(gestorResumoService.buscarResumo()).thenReturn(resumo);

        ResponseEntity<ResumoOperacaoResponse> response = gestorResumoController.buscarResumo();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(resumo, response.getBody());
        verify(gestorResumoService).buscarResumo();
    }
}
