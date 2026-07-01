package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.service.NotificacaoService;
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
class NotificacaoControllerTest {

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private NotificacaoController notificacaoController;

    @Test
    void criarNotificacaoDeveRetornarCreated() {
        CriarNotificacaoRequest request = criarRequest();
        NotificacaoResponse notificacaoResponse = criarResponse(false);

        when(notificacaoService.criarNotificacao(request)).thenReturn(notificacaoResponse);

        ResponseEntity<NotificacaoResponse> response = notificacaoController.criarNotificacao(request);

        assertEquals(201, response.getStatusCode().value());
        assertSame(notificacaoResponse, response.getBody());
        verify(notificacaoService).criarNotificacao(request);
    }

    @Test
    void listarPendentesDeveRetornarOk() {
        NotificacaoResponse notificacaoResponse = criarResponse(false);

        when(notificacaoService.listarPendentes(DestinoNotificacao.COZINHA)).thenReturn(List.of(notificacaoResponse));

        ResponseEntity<List<NotificacaoResponse>> response = notificacaoController.listarPendentes(DestinoNotificacao.COZINHA);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(notificacaoResponse), response.getBody());
        verify(notificacaoService).listarPendentes(DestinoNotificacao.COZINHA);
    }

    @Test
    void marcarComoLidaDeveRetornarOk() {
        NotificacaoResponse notificacaoResponse = criarResponse(true);

        when(notificacaoService.marcarComoLida(1)).thenReturn(notificacaoResponse);

        ResponseEntity<NotificacaoResponse> response = notificacaoController.marcarComoLida(1);

        assertEquals(200, response.getStatusCode().value());
        assertSame(notificacaoResponse, response.getBody());
        verify(notificacaoService).marcarComoLida(1);
    }

    private CriarNotificacaoRequest criarRequest() {
        return new CriarNotificacaoRequest(
                "PEDIDO_PRONTO",
                "Pedido pronto para retirada",
                DestinoNotificacao.COZINHA
        );
    }

    private NotificacaoResponse criarResponse(boolean lida) {
        return new NotificacaoResponse(
                1,
                "PEDIDO_PRONTO",
                "Pedido pronto para retirada",
                DestinoNotificacao.COZINHA,
                lida,
                LocalDateTime.of(2026, 7, 1, 13, 25)
        );
    }
}
