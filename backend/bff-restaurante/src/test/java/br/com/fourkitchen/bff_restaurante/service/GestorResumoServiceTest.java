package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.ResumoMesasOperacaoResponse;
import br.com.fourkitchen.bff_restaurante.client.notificacoes.NotificacaoClient;
import br.com.fourkitchen.bff_restaurante.client.notificacoes.dto.ResumoNotificacoesOperacaoResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ResumoPedidosOperacaoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ResumoOperacaoResponse;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorResumoServiceTest {

    @Mock
    private PedidoClient pedidoClient;

    @Mock
    private MesaClient mesaClient;

    @Mock
    private NotificacaoClient notificacaoClient;

    @InjectMocks
    private GestorResumoService gestorResumoService;

    @Test
    void buscarResumoDeveAgregarDadosDosMicroservicos() {
        when(pedidoClient.buscarResumoOperacao()).thenReturn(new ResumoPedidosOperacaoResponse(5L, 3L, 2L));
        when(mesaClient.buscarResumoOperacao()).thenReturn(new ResumoMesasOperacaoResponse(8L));
        when(notificacaoClient.buscarResumoOperacao()).thenReturn(new ResumoNotificacoesOperacaoResponse(4L));

        ResumoOperacaoResponse resultado = gestorResumoService.buscarResumo();

        assertEquals(5L, resultado.pedidosEmPreparo());
        assertEquals(3L, resultado.pedidosProntos());
        assertEquals(8L, resultado.mesasOcupadas());
        assertEquals(2L, resultado.problemasPendentes());
        assertEquals(4L, resultado.chamadasPendentes());
        verify(pedidoClient).buscarResumoOperacao();
        verify(mesaClient).buscarResumoOperacao();
        verify(notificacaoClient).buscarResumoOperacao();
    }

    @Test
    void buscarResumoDeveMapearMsPedidosIndisponivel() {
        when(pedidoClient.buscarResumoOperacao()).thenThrow(feignException(500));

        BaseException exception = assertThrows(BaseException.class, () -> gestorResumoService.buscarResumo());

        assertEquals(ErrorEnum.MS_PEDIDOS_INDISPONIVEL, exception.getErrorEnum());
        verify(pedidoClient).buscarResumoOperacao();
        verify(mesaClient, never()).buscarResumoOperacao();
        verify(notificacaoClient, never()).buscarResumoOperacao();
    }

    @Test
    void buscarResumoDeveMapearMsMesasIndisponivel() {
        when(pedidoClient.buscarResumoOperacao()).thenReturn(new ResumoPedidosOperacaoResponse(5L, 3L, 2L));
        when(mesaClient.buscarResumoOperacao()).thenThrow(feignException(500));

        BaseException exception = assertThrows(BaseException.class, () -> gestorResumoService.buscarResumo());

        assertEquals(ErrorEnum.MS_MESAS_INDISPONIVEL, exception.getErrorEnum());
        verify(pedidoClient).buscarResumoOperacao();
        verify(mesaClient).buscarResumoOperacao();
        verify(notificacaoClient, never()).buscarResumoOperacao();
    }

    @Test
    void buscarResumoDeveMapearMsNotificacoesIndisponivel() {
        when(pedidoClient.buscarResumoOperacao()).thenReturn(new ResumoPedidosOperacaoResponse(5L, 3L, 2L));
        when(mesaClient.buscarResumoOperacao()).thenReturn(new ResumoMesasOperacaoResponse(8L));
        when(notificacaoClient.buscarResumoOperacao()).thenThrow(feignException(500));

        BaseException exception = assertThrows(BaseException.class, () -> gestorResumoService.buscarResumo());

        assertEquals(ErrorEnum.MS_NOTIFICACOES_INDISPONIVEL, exception.getErrorEnum());
        verify(pedidoClient).buscarResumoOperacao();
        verify(mesaClient).buscarResumoOperacao();
        verify(notificacaoClient).buscarResumoOperacao();
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/resumo-operacao",
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

        return FeignException.errorStatus("resumo-operacao", response);
    }
}
