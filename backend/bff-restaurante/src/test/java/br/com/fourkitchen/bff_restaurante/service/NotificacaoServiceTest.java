package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.notificacoes.NotificacaoClient;
import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.TipoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private NotificacaoClient notificacaoClient;

    @InjectMocks
    private NotificacaoService notificacaoService;

    @Test
    void criarNotificacaoDeveDelegarParaMsNotificacoes() {
        CriarNotificacaoRequest request = criarRequest();
        NotificacaoResponse response = criarResponse(false);

        when(notificacaoClient.criarNotificacao(request)).thenReturn(response);

        NotificacaoResponse resultado = notificacaoService.criarNotificacao(request);

        assertSame(response, resultado);
        verify(notificacaoClient).criarNotificacao(request);
    }

    @Test
    void listarPendentesDeveDelegarParaMsNotificacoes() {
        NotificacaoResponse response = criarResponse(false);

        when(notificacaoClient.listarPendentes(DestinoNotificacao.COZINHA)).thenReturn(List.of(response));

        List<NotificacaoResponse> resultado = notificacaoService.listarPendentes(DestinoNotificacao.COZINHA);

        assertEquals(List.of(response), resultado);
        verify(notificacaoClient).listarPendentes(DestinoNotificacao.COZINHA);
    }

    @Test
    void marcarComoLidaDeveDelegarParaMsNotificacoes() {
        NotificacaoResponse response = criarResponse(true);

        when(notificacaoClient.marcarComoLida(1)).thenReturn(response);

        NotificacaoResponse resultado = notificacaoService.marcarComoLida(1);

        assertSame(response, resultado);
        verify(notificacaoClient).marcarComoLida(1);
    }

    @Test
    void marcarComoLidaDeveMapearNotificacaoNaoEncontrada() {
        when(notificacaoClient.marcarComoLida(99)).thenThrow(feignException(404));

        BaseException exception = assertThrows(BaseException.class, () -> notificacaoService.marcarComoLida(99));

        assertEquals(ErrorEnum.NOTIFICACAO_NAO_ENCONTRADA, exception.getErrorEnum());
        verify(notificacaoClient).marcarComoLida(99);
    }

    @Test
    void concluirChamadaGarcomDeveDelegarParaMsNotificacoes() {
        NotificacaoResponse response = criarResponse(true);

        when(notificacaoClient.concluirChamadaGarcom(3, 7)).thenReturn(response);

        NotificacaoResponse resultado = notificacaoService.concluirChamadaGarcom(3, 7);

        assertSame(response, resultado);
        verify(notificacaoClient).concluirChamadaGarcom(3, 7);
    }

    @Test
    void concluirChamadaGarcomDeveMapearNotificacaoNaoEncontrada() {
        when(notificacaoClient.concluirChamadaGarcom(99, 7)).thenThrow(feignException(404));

        BaseException exception = assertThrows(BaseException.class, () -> notificacaoService.concluirChamadaGarcom(99, 7));

        assertEquals(ErrorEnum.NOTIFICACAO_NAO_ENCONTRADA, exception.getErrorEnum());
        verify(notificacaoClient).concluirChamadaGarcom(99, 7);
    }

    @Test
    void concluirChamadaGarcomDeveMapearChamadaInvalida() {
        when(notificacaoClient.concluirChamadaGarcom(3, 7)).thenThrow(feignException(400));

        BaseException exception = assertThrows(BaseException.class, () -> notificacaoService.concluirChamadaGarcom(3, 7));

        assertEquals(ErrorEnum.CHAMADA_GARCOM_INVALIDA, exception.getErrorEnum());
        verify(notificacaoClient).concluirChamadaGarcom(3, 7);
    }

    @Test
    void concluirChamadaGarcomDeveMapearChamadaDeOutroGarcom() {
        when(notificacaoClient.concluirChamadaGarcom(3, 9)).thenThrow(feignException(403));

        BaseException exception = assertThrows(BaseException.class, () -> notificacaoService.concluirChamadaGarcom(3, 9));

        assertEquals(ErrorEnum.CHAMADA_GARCOM_NAO_PERTENCE_AO_GARCOM, exception.getErrorEnum());
        verify(notificacaoClient).concluirChamadaGarcom(3, 9);
    }

    @Test
    void listarPendentesDeveMapearMsNotificacoesIndisponivel() {
        when(notificacaoClient.listarPendentes(null)).thenThrow(feignException(500));

        BaseException exception = assertThrows(BaseException.class, () -> notificacaoService.listarPendentes(null));

        assertEquals(ErrorEnum.MS_NOTIFICACOES_INDISPONIVEL, exception.getErrorEnum());
        verify(notificacaoClient).listarPendentes(null);
    }

    private CriarNotificacaoRequest criarRequest() {
        return new CriarNotificacaoRequest(
                TipoNotificacao.PEDIDO_PRONTO,
                DestinoNotificacao.COZINHA,
                null,
                null,
                null
        );
    }

    private NotificacaoResponse criarResponse(boolean lida) {
        return new NotificacaoResponse(
                1,
                "PEDIDO_PRONTO",
                "Pedido pronto para retirada",
                DestinoNotificacao.COZINHA,
                lida,
                LocalDateTime.of(2026, 7, 1, 13, 25),
                null,
                null,
                null
        );
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/notificacoes/pendentes",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null
        );

        Response response = Response.builder()
                .status(status)
                .reason("Erro")
                .request(request)
                .build();

        return FeignException.errorStatus("notificacoes", response);
    }
}
