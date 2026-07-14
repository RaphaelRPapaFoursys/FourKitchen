package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaGarcomClientResponse;
import br.com.fourkitchen.bff_restaurante.client.notificacoes.NotificacaoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaProblemasGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.mapper.MesaGarcomMapperSource;
import br.com.fourkitchen.bff_restaurante.mapper.MesaGarcomResponseMapper;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GarcomMesaServiceTest {

    @Mock
    private MesaClient mesaClient;

    @Mock
    private PedidoClient pedidoClient;

    @Mock
    private NotificacaoClient notificacaoClient;

    @Mock
    private MesaGarcomResponseMapper mesaGarcomResponseMapper;

    @InjectMocks
    private GarcomMesaService garcomMesaService;

    @Test
    void listarMesasDeveBuscarMesasDoGarcomEAgregarPedidosEChamadas() {
        Authentication authentication = criarAuthentication(7L);
        MesaGarcomClientResponse mesa = criarMesa();
        PedidoResponse pedido = new PedidoResponse(25, 100025, "GARCOM", "ENVIADO_COZINHA", 1, 7, 8);
        NotificacaoResponse chamada = criarChamada();
        MesaGarcomResponse response = criarResponse();

        when(mesaClient.listarMesasPorGarcom(7)).thenReturn(List.of(mesa));
        when(pedidoClient.listarPedidosAtivosPorAtendimentos(List.of(8))).thenReturn(List.of(pedido));
        when(notificacaoClient.listarChamadasPendentesPorAtendimentos(List.of(8))).thenReturn(List.of(chamada));
        when(mesaGarcomResponseMapper.map(any(MesaGarcomMapperSource.class))).thenReturn(response);

        List<MesaGarcomResponse> resultado = garcomMesaService.listarMesas(authentication);

        assertEquals(List.of(response), resultado);
        verify(mesaClient).listarMesasPorGarcom(7);
        verify(pedidoClient).listarPedidosAtivosPorAtendimentos(List.of(8));
        verify(notificacaoClient).listarChamadasPendentesPorAtendimentos(List.of(8));

        ArgumentCaptor<MesaGarcomMapperSource> sourceCaptor = ArgumentCaptor.forClass(MesaGarcomMapperSource.class);
        verify(mesaGarcomResponseMapper).map(sourceCaptor.capture());

        MesaGarcomMapperSource source = sourceCaptor.getValue();
        assertSame(mesa, source.mesa());
        assertEquals(List.of(pedido), source.pedidosAtivos());
        assertEquals(List.of(chamada), source.chamadasPendentes());
    }

    @Test
    void listarMesasDeveRetornarListaVaziaSemBuscarPedidosQuandoGarcomNaoTemMesas() {
        Authentication authentication = criarAuthentication(7L);

        when(mesaClient.listarMesasPorGarcom(7)).thenReturn(List.of());

        List<MesaGarcomResponse> resultado = garcomMesaService.listarMesas(authentication);

        assertEquals(List.of(), resultado);
        verify(mesaClient).listarMesasPorGarcom(7);
        verifyNoInteractions(pedidoClient, notificacaoClient, mesaGarcomResponseMapper);
    }

    @Test
    void listarMesasDeveMapearMesaSemAtendimentoSemBuscarPedidosEChamadas() {
        Authentication authentication = criarAuthentication(7L);
        MesaGarcomClientResponse mesa = new MesaGarcomClientResponse(
                1,
                10,
                "OCUPADA",
                null,
                123456,
                7,
                LocalDateTime.of(2026, 7, 2, 10, 0)
        );
        MesaGarcomResponse response = criarResponse();

        when(mesaClient.listarMesasPorGarcom(7)).thenReturn(List.of(mesa));
        when(mesaGarcomResponseMapper.map(any(MesaGarcomMapperSource.class))).thenReturn(response);

        List<MesaGarcomResponse> resultado = garcomMesaService.listarMesas(authentication);

        assertEquals(List.of(response), resultado);
        verify(mesaClient).listarMesasPorGarcom(7);
        verifyNoInteractions(pedidoClient, notificacaoClient);

        ArgumentCaptor<MesaGarcomMapperSource> sourceCaptor = ArgumentCaptor.forClass(MesaGarcomMapperSource.class);
        verify(mesaGarcomResponseMapper).map(sourceCaptor.capture());
        assertEquals(List.of(), sourceCaptor.getValue().pedidosAtivos());
        assertEquals(List.of(), sourceCaptor.getValue().chamadasPendentes());
    }

    @Test
    void listarMesasDeveMapearFalhaDoMsMesas() {
        Authentication authentication = criarAuthentication(7L);

        when(mesaClient.listarMesasPorGarcom(7)).thenThrow(feignException(500));

        BaseException exception = assertThrows(BaseException.class, () -> garcomMesaService.listarMesas(authentication));

        assertEquals(ErrorEnum.MS_MESAS_INDISPONIVEL, exception.getErrorEnum());
        verify(mesaClient).listarMesasPorGarcom(7);
        verifyNoInteractions(pedidoClient, notificacaoClient, mesaGarcomResponseMapper);
    }

    @Test
    void listarMesasDeveMapearDadosInvalidosQuandoMsMesasRetornar400() {
        Authentication authentication = criarAuthentication(7L);

        when(mesaClient.listarMesasPorGarcom(7)).thenThrow(feignException(400));

        BaseException exception = assertThrows(BaseException.class, () -> garcomMesaService.listarMesas(authentication));

        assertEquals(ErrorEnum.DADOS_INVALIDOS, exception.getErrorEnum());
        verify(mesaClient).listarMesasPorGarcom(7);
        verifyNoInteractions(pedidoClient, notificacaoClient, mesaGarcomResponseMapper);
    }

    @Test
    void listarMesasDeveMapearFalhaDoMsPedidos() {
        Authentication authentication = criarAuthentication(7L);
        MesaGarcomClientResponse mesa = criarMesa();

        when(mesaClient.listarMesasPorGarcom(7)).thenReturn(List.of(mesa));
        when(pedidoClient.listarPedidosAtivosPorAtendimentos(List.of(8))).thenThrow(feignException(500));

        BaseException exception = assertThrows(BaseException.class, () -> garcomMesaService.listarMesas(authentication));

        assertEquals(ErrorEnum.MS_PEDIDOS_INDISPONIVEL, exception.getErrorEnum());
        verify(mesaClient).listarMesasPorGarcom(7);
        verify(pedidoClient).listarPedidosAtivosPorAtendimentos(List.of(8));
        verifyNoInteractions(notificacaoClient, mesaGarcomResponseMapper);
    }

    @Test
    void listarMesasDeveMapearFalhaDoMsNotificacoes() {
        Authentication authentication = criarAuthentication(7L);
        MesaGarcomClientResponse mesa = criarMesa();

        when(mesaClient.listarMesasPorGarcom(7)).thenReturn(List.of(mesa));
        when(pedidoClient.listarPedidosAtivosPorAtendimentos(List.of(8))).thenReturn(List.of());
        when(notificacaoClient.listarChamadasPendentesPorAtendimentos(List.of(8))).thenThrow(feignException(500));

        BaseException exception = assertThrows(BaseException.class, () -> garcomMesaService.listarMesas(authentication));

        assertEquals(ErrorEnum.MS_NOTIFICACOES_INDISPONIVEL, exception.getErrorEnum());
        verify(mesaClient).listarMesasPorGarcom(7);
        verify(pedidoClient).listarPedidosAtivosPorAtendimentos(List.of(8));
        verify(notificacaoClient).listarChamadasPendentesPorAtendimentos(List.of(8));
        verifyNoInteractions(mesaGarcomResponseMapper);
    }

    @Test
    void listarProblemasDaMesaDeveBuscarSomentePedidosSemConsultarResumoDaConta() {
        Authentication authentication = criarAuthentication(7L);
        PedidoCozinhaResponse aguardandoDecisao = criarPedidoDetalhado(
                25,
                "AGUARDANDO_DECISAO",
                "FALTA_PRODUTO"
        );
        PedidoCozinhaResponse emPreparo = criarPedidoDetalhado(26, "EM_PREPARO", null);

        when(mesaClient.listarMesasPorGarcom(7)).thenReturn(List.of(criarMesa()));
        when(pedidoClient.listarPedidosDetalhadosPorAtendimento(8))
                .thenReturn(List.of(aguardandoDecisao, emPreparo));

        MesaProblemasGarcomResponse resultado = garcomMesaService.listarProblemasDaMesa(1, authentication);

        assertEquals(1, resultado.pedidos().size());
        assertEquals(25, resultado.pedidos().getFirst().id());
        assertEquals(1, resultado.problemas().size());
        assertEquals(80, resultado.problemas().getFirst().idProdutoPedido());
        verify(pedidoClient, never()).buscarResumoContaAtendimento(8);
    }

    @Test
    void marcarPedidoComoEntregueDeveValidarMesaEPedidoProntoAntesDeEntregar() {
        Authentication authentication = criarAuthentication(7L);
        PedidoCozinhaResponse pedidoPronto = criarPedidoDetalhado(25, "PRONTO", null);

        when(mesaClient.listarMesasPorGarcom(7)).thenReturn(List.of(criarMesa()));
        when(pedidoClient.listarPedidosDetalhadosPorAtendimento(8)).thenReturn(List.of(pedidoPronto));

        garcomMesaService.marcarPedidoComoEntregue(1, 25, authentication);

        verify(pedidoClient).entregarPedido(25);
    }

    @Test
    void marcarPedidoComoEntregueDeveRecusarPedidoQueNaoEstaPronto() {
        Authentication authentication = criarAuthentication(7L);
        PedidoCozinhaResponse pedidoEmPreparo = criarPedidoDetalhado(25, "EM_PREPARO", null);

        when(mesaClient.listarMesasPorGarcom(7)).thenReturn(List.of(criarMesa()));
        when(pedidoClient.listarPedidosDetalhadosPorAtendimento(8)).thenReturn(List.of(pedidoEmPreparo));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> garcomMesaService.marcarPedidoComoEntregue(1, 25, authentication)
        );

        assertEquals(ErrorEnum.TRANSICAO_STATUS_INVALIDA, exception.getErrorEnum());
        verify(pedidoClient, never()).entregarPedido(25);
    }

    @Test
    void listarMesasDeveLancarTokenInvalidoQuandoAuthenticationForNulo() {
        BaseException exception = assertThrows(BaseException.class, () -> garcomMesaService.listarMesas(null));

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
        verifyNoInteractions(mesaClient, pedidoClient, notificacaoClient, mesaGarcomResponseMapper);
    }

    @Test
    void listarMesasDeveLancarTokenInvalidoQuandoPrincipalNaoForUsuarioAutenticado() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("garcom", null, List.of());

        BaseException exception = assertThrows(BaseException.class, () -> garcomMesaService.listarMesas(authentication));

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
        verifyNoInteractions(mesaClient, pedidoClient, notificacaoClient, mesaGarcomResponseMapper);
    }

    @Test
    void listarMesasDeveLancarTokenInvalidoQuandoIdGarcomForNulo() {
        Authentication authentication = criarAuthentication(null);

        BaseException exception = assertThrows(BaseException.class, () -> garcomMesaService.listarMesas(authentication));

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
        verifyNoInteractions(mesaClient, pedidoClient, notificacaoClient, mesaGarcomResponseMapper);
    }

    @Test
    void listarMesasDeveLancarDadosInvalidosQuandoIdGarcomNaoCouberEmInteger() {
        Authentication authentication = criarAuthentication((long) Integer.MAX_VALUE + 1);

        BaseException exception = assertThrows(BaseException.class, () -> garcomMesaService.listarMesas(authentication));

        assertEquals(ErrorEnum.DADOS_INVALIDOS, exception.getErrorEnum());
        verifyNoInteractions(mesaClient, pedidoClient, notificacaoClient, mesaGarcomResponseMapper);
    }

    private Authentication criarAuthentication(Long idGarcom) {
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                idGarcom,
                "Amanda",
                "amanda@fourkitchen.com",
                "GARCOM",
                null
        );

        return new UsernamePasswordAuthenticationToken(usuario, null, List.of());
    }

    private MesaGarcomClientResponse criarMesa() {
        return new MesaGarcomClientResponse(
                1,
                10,
                "OCUPADA",
                8,
                123456,
                7,
                LocalDateTime.of(2026, 7, 2, 10, 0)
        );
    }

    private NotificacaoResponse criarChamada() {
        return new NotificacaoResponse(
                3,
                "CHAMADA_GARCOM",
                "Cliente solicitou atendimento",
                DestinoNotificacao.GARCOM,
                false,
                LocalDateTime.of(2026, 7, 2, 10, 15),
                1,
                8,
                7
        );
    }

    private MesaGarcomResponse criarResponse() {
        return new MesaGarcomResponse(
                1,
                10,
                "OCUPADA",
                8,
                123456,
                7,
                LocalDateTime.of(2026, 7, 2, 10, 0),
                List.of(),
                List.of(),
                true
        );
    }

    private PedidoCozinhaResponse criarPedidoDetalhado(
            Integer id,
            String statusPedido,
            String statusItem
    ) {
        return new PedidoCozinhaResponse(
                id,
                100000 + id,
                "MESA",
                statusPedido,
                1,
                8,
                LocalDateTime.of(2026, 7, 2, 10, 30),
                null,
                null,
                List.of(new ItemPedidoCozinhaResponse(
                        80,
                        4,
                        "Hamburguer",
                        1,
                        new BigDecimal("25.00"),
                        null,
                        statusItem
                ))
        );
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/mesas/garcons/7",
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

        return FeignException.errorStatus("listarMesasPorGarcom", response);
    }
}
