package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.request.ChamarGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.service.MesaChamadaGarcomService;
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
class MesaChamadaGarcomControllerTest {

    @Mock
    private MesaChamadaGarcomService mesaChamadaGarcomService;

    @InjectMocks
    private MesaChamadaGarcomController mesaChamadaGarcomController;

    @Test
    void chamarGarcomDeveRetornarCreated() {
        ChamarGarcomRequest request = new ChamarGarcomRequest(123456);
        Authentication authentication = mock(Authentication.class);
        NotificacaoResponse notificacao = new NotificacaoResponse(
                3,
                "CHAMADA_GARCOM",
                "Cliente solicitou atendimento",
                DestinoNotificacao.GARCOM,
                false,
                LocalDateTime.of(2026, 7, 2, 10, 15, 30),
                1,
                8,
                7
        );

        when(mesaChamadaGarcomService.chamarGarcom(request, authentication)).thenReturn(notificacao);

        ResponseEntity<NotificacaoResponse> response = mesaChamadaGarcomController.chamarGarcom(request, authentication);

        assertEquals(201, response.getStatusCode().value());
        assertSame(notificacao, response.getBody());
        verify(mesaChamadaGarcomService).chamarGarcom(request, authentication);
    }
}
