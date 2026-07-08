package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.MesaAtendimentoAtualResponse;
import br.com.fourkitchen.bff_restaurante.service.MesaAtendimentoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MesaAtendimentoControllerTest {

    @Mock
    private MesaAtendimentoService mesaAtendimentoService;

    @InjectMocks
    private MesaAtendimentoController mesaAtendimentoController;

    @Test
    void buscarAtendimentoAtualDeveRetornarOk() {
        Authentication authentication = mock(Authentication.class);
        MesaAtendimentoAtualResponse atendimento = new MesaAtendimentoAtualResponse(
                1,
                8,
                123456,
                "OCUPADA"
        );

        when(mesaAtendimentoService.buscarAtendimentoAtual(authentication)).thenReturn(atendimento);

        ResponseEntity<MesaAtendimentoAtualResponse> response =
                mesaAtendimentoController.buscarAtendimentoAtual(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertSame(atendimento, response.getBody());
        verify(mesaAtendimentoService).buscarAtendimentoAtual(authentication);
    }
}
