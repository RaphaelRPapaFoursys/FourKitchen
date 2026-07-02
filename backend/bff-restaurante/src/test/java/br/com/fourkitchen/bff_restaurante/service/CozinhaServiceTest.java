package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.TipoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoFilaCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoStatusCozinhaResponse;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CozinhaServiceTest {

    @Mock
    private PedidoClient pedidoClient;

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private CozinhaService cozinhaService;

    @Test
    void listarFilaDeveDelegarParaMsPedidosEMapearResponse() {
        PedidoCozinhaResponse response = criarResponse();

        when(pedidoClient.listarFilaCozinha()).thenReturn(List.of(response));

        List<PedidoFilaCozinhaResponse> resultado = cozinhaService.listarFila();

        assertEquals(1, resultado.size());
        PedidoFilaCozinhaResponse pedido = resultado.getFirst();
        assertEquals(25, pedido.id());
        assertEquals(123456, pedido.codigo());
        assertEquals("MESA", pedido.canal());
        assertEquals("ENVIADO_COZINHA", pedido.status());
        assertEquals(1, pedido.idMesa());
        assertEquals(8, pedido.idAtendimento());
        assertEquals(LocalDateTime.of(2026, 7, 2, 10, 30), pedido.dataCriacao());
        assertEquals(1, pedido.itens().size());
        assertEquals("Sem cebola", pedido.itens().getFirst().observacao());
        verify(pedidoClient).listarFilaCozinha();
    }

    @Test
    void iniciarPreparoDeveAlterarStatusERegistrarEvento() {
        PedidoResponse response = criarPedidoResponse("EM_PREPARO");

        when(pedidoClient.iniciarPreparo(25)).thenReturn(response);

        PedidoStatusCozinhaResponse resultado = cozinhaService.iniciarPreparo(25);

        assertEquals(25, resultado.id());
        assertEquals(123456, resultado.codigo());
        assertEquals("GARCOM", resultado.canal());
        assertEquals("EM_PREPARO", resultado.status());
        assertEquals(1, resultado.idMesa());
        assertEquals(8, resultado.idAtendimento());
        verify(pedidoClient).iniciarPreparo(25);
        verify(notificacaoService).criarNotificacao(new CriarNotificacaoRequest(
                TipoNotificacao.PEDIDO_EM_PREPARO,
                DestinoNotificacao.COZINHA,
                null,
                null,
                null
        ));
    }

    @Test
    void finalizarPreparoDeveAlterarStatusERegistrarEvento() {
        PedidoResponse response = criarPedidoResponse("PRONTO");

        when(pedidoClient.finalizarPreparo(25)).thenReturn(response);

        PedidoStatusCozinhaResponse resultado = cozinhaService.finalizarPreparo(25);

        assertEquals(25, resultado.id());
        assertEquals(123456, resultado.codigo());
        assertEquals("PRONTO", resultado.status());
        verify(pedidoClient).finalizarPreparo(25);
        verify(notificacaoService).criarNotificacao(new CriarNotificacaoRequest(
                TipoNotificacao.PEDIDO_PRONTO,
                DestinoNotificacao.GARCOM,
                null,
                null,
                null
        ));
    }

    @Test
    void iniciarPreparoDeveMapearTransicaoInvalidaSemRegistrarEvento() {
        when(pedidoClient.iniciarPreparo(25)).thenThrow(feignException(400));

        BaseException exception = assertThrows(BaseException.class, () -> cozinhaService.iniciarPreparo(25));

        assertEquals(ErrorEnum.TRANSICAO_STATUS_INVALIDA, exception.getErrorEnum());
        verify(pedidoClient).iniciarPreparo(25);
        verify(notificacaoService, never()).criarNotificacao(any());
    }

    @Test
    void finalizarPreparoDeveMapearPedidoNaoEncontradoSemRegistrarEvento() {
        when(pedidoClient.finalizarPreparo(25)).thenThrow(feignException(404));

        BaseException exception = assertThrows(BaseException.class, () -> cozinhaService.finalizarPreparo(25));

        assertEquals(ErrorEnum.PEDIDO_NAO_ENCONTRADO, exception.getErrorEnum());
        verify(pedidoClient).finalizarPreparo(25);
        verify(notificacaoService, never()).criarNotificacao(any());
    }

    @Test
    void listarFilaDeveMapearMsPedidosIndisponivel() {
        when(pedidoClient.listarFilaCozinha()).thenThrow(feignException(500));

        BaseException exception = assertThrows(BaseException.class, () -> cozinhaService.listarFila());

        assertEquals(ErrorEnum.MS_PEDIDOS_INDISPONIVEL, exception.getErrorEnum());
        verify(pedidoClient).listarFilaCozinha();
    }

    private PedidoCozinhaResponse criarResponse() {
        return new PedidoCozinhaResponse(
                25,
                123456,
                "MESA",
                "ENVIADO_COZINHA",
                1,
                8,
                LocalDateTime.of(2026, 7, 2, 10, 30),
                List.of(new ItemPedidoCozinhaResponse(
                        5,
                        10,
                        2,
                        new BigDecimal("29.90"),
                        "Sem cebola"
                ))
        );
    }

    private PedidoResponse criarPedidoResponse(String status) {
        return new PedidoResponse(
                25,
                123456,
                "GARCOM",
                status,
                1,
                7,
                8
        );
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/pedidos/cozinha/fila",
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

        return FeignException.errorStatus("pedidos", response);
    }
}
