package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.service.GarcomChamadaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GarcomChamadaControllerTest {

    @Mock
    private GarcomChamadaService garcomChamadaService;

    @InjectMocks
    private GarcomChamadaController garcomChamadaController;

    @Test
    void concluirChamadaDeveRetornarOk() {
        Authentication authentication = mock(Authentication.class);
        NotificacaoResponse notificacaoResponse = new NotificacaoResponse(
                3,
                "CHAMADA_GARCOM",
                "Cliente solicitou atendimento",
                DestinoNotificacao.GARCOM,
                true,
                LocalDateTime.of(2026, 7, 2, 10, 15),
                1,
                8,
                7
        );

        when(garcomChamadaService.concluirChamada(3, authentication)).thenReturn(notificacaoResponse);

        ResponseEntity<NotificacaoResponse> response = garcomChamadaController.concluirChamada(3, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertSame(notificacaoResponse, response.getBody());
        verify(garcomChamadaService).concluirChamada(3, authentication);
    }
}
