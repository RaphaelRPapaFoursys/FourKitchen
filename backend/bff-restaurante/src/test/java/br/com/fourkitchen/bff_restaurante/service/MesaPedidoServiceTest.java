package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.SessaoMesaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.CriarPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoMesaRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.ItemPedidoMesaRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoMesaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoMesaStatusResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MesaPedidoServiceTest {

    @Mock
    private MesaClient mesaClient;

    @Mock
    private PedidoClient pedidoClient;

    @Mock
    private ProdutoClient produtoClient;

    @InjectMocks
    private MesaPedidoService mesaPedidoService;

    @Test
    void criarPedidoDeveValidarSessaoECriarPedidoComoMesa() {
        CriarPedidoMesaRequest request = criarRequest();
        SessaoMesaResponse sessao = new SessaoMesaResponse(1, 8, 123456, 7, "OCUPADA");
        br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse pedidoResponse =
                new br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse(
                        25,
                        100025,
                        "MESA",
                        "ENVIADO_COZINHA",
                        1,
                        101,
                        8
                );

        when(mesaClient.validarSessaoMesa(1, 123456)).thenReturn(sessao);
        when(produtoClient.verificarDisponibilidade(10))
                .thenReturn(new ProdutoDisponibilidadeResponse(10, "X-Burger", true, new BigDecimal("29.90")));
        when(pedidoClient.criarPedido(any(CriarPedidoRequest.class))).thenReturn(pedidoResponse);

        PedidoMesaResponse response = mesaPedidoService.criarPedido(request, criarAuthenticationMesa());

        assertEquals(25, response.id());
        assertEquals(100025, response.codigo());
        assertEquals("MESA", response.canal());
        assertEquals("ENVIADO_COZINHA", response.status());
        assertEquals(1, response.idMesa());
        assertEquals(8, response.idAtendimento());

        ArgumentCaptor<CriarPedidoRequest> pedidoRequestCaptor = ArgumentCaptor.forClass(CriarPedidoRequest.class);
        verify(pedidoClient).criarPedido(pedidoRequestCaptor.capture());

        CriarPedidoRequest pedidoRequest = pedidoRequestCaptor.getValue();
        assertEquals("MESA", pedidoRequest.canal());
        assertEquals("ENVIADO_COZINHA", pedidoRequest.status());
        assertEquals(1, pedidoRequest.idMesa());
        assertEquals(8, pedidoRequest.idAtendimento());
        assertEquals(101, pedidoRequest.idUsuario());
        assertEquals(1, pedidoRequest.itens().size());
        assertEquals(10, pedidoRequest.itens().getFirst().idProduto());
        assertEquals("X-Burger", pedidoRequest.itens().getFirst().nomeProduto());
        assertEquals(new BigDecimal("29.90"), pedidoRequest.itens().getFirst().precoUnitario());
        verify(mesaClient).validarSessaoMesa(1, 123456);
        verify(produtoClient).verificarDisponibilidade(10);
    }

    @Test
    void criarPedidoDeveBloquearSessaoMesaInvalida() {
        CriarPedidoMesaRequest request = criarRequest();

        when(mesaClient.validarSessaoMesa(1, 123456)).thenThrow(feignException(404));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> mesaPedidoService.criarPedido(request, criarAuthenticationMesa())
        );

        assertEquals(ErrorEnum.SESSAO_MESA_INVALIDA, exception.getErrorEnum());
        verify(mesaClient).validarSessaoMesa(1, 123456);
        verifyNoInteractions(produtoClient, pedidoClient);
    }

    @Test
    void criarPedidoDeveLancarServicoPedidosIndisponivelQuandoMsPedidosFalhar() {
        CriarPedidoMesaRequest request = criarRequest();
        SessaoMesaResponse sessao = new SessaoMesaResponse(1, 8, 123456, 7, "OCUPADA");

        when(mesaClient.validarSessaoMesa(1, 123456)).thenReturn(sessao);
        when(produtoClient.verificarDisponibilidade(10))
                .thenReturn(new ProdutoDisponibilidadeResponse(10, "X-Burger", true, new BigDecimal("29.90")));
        when(pedidoClient.criarPedido(any(CriarPedidoRequest.class))).thenThrow(feignException(500));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> mesaPedidoService.criarPedido(request, criarAuthenticationMesa())
        );

        assertEquals(ErrorEnum.MS_PEDIDOS_INDISPONIVEL, exception.getErrorEnum());
        verify(mesaClient).validarSessaoMesa(1, 123456);
        verify(produtoClient).verificarDisponibilidade(10);
        verify(pedidoClient).criarPedido(any(CriarPedidoRequest.class));
    }

    @Test
    void criarPedidoDeveBloquearUsuarioMesaSemVinculo() {
        CriarPedidoMesaRequest request = criarRequest();

        UsuarioAutenticado usuario = new UsuarioAutenticado(
                101L,
                "Mesa sem vinculo",
                "mesa@fourkitchen.com",
                "MESA",
                null
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, List.of());

        BaseException exception = assertThrows(
                BaseException.class,
                () -> mesaPedidoService.criarPedido(request, authentication)
        );

        assertEquals(ErrorEnum.ACESSO_NEGADO, exception.getErrorEnum());
        verifyNoInteractions(mesaClient, produtoClient, pedidoClient);
    }

    @Test
    void criarPedidoDeveBloquearPerfilDiferenteDeMesa() {
        CriarPedidoMesaRequest request = criarRequest();

        UsuarioAutenticado usuario = new UsuarioAutenticado(
                101L,
                "Garcom",
                "garcom01",
                "GARCOM",
                1
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, List.of());

        BaseException exception = assertThrows(
                BaseException.class,
                () -> mesaPedidoService.criarPedido(request, authentication)
        );

        assertEquals(ErrorEnum.ACESSO_NEGADO, exception.getErrorEnum());
        verifyNoInteractions(mesaClient, produtoClient, pedidoClient);
    }

    @Test
    void listarPedidosDoAtendimentoAtualDeveValidarSessaoEMapearPedidos() {
        SessaoMesaResponse sessao = new SessaoMesaResponse(1, 8, 123456, 7, "OCUPADA");
        LocalDateTime dataCriacao = LocalDateTime.of(2026, 7, 2, 10, 30);
        PedidoCozinhaResponse pedido = new PedidoCozinhaResponse(
                25,
                100025,
                "MESA",
                "PROBLEMA_COZINHA",
                1,
                8,
                dataCriacao,
                List.of(new ItemPedidoCozinhaResponse(
                        5,
                        10,
                        "Produto teste",
                        2,
                        new BigDecimal("29.90"),
                        "Sem cebola"
                ))
        );

        when(mesaClient.validarSessaoMesa(1, 123456)).thenReturn(sessao);
        when(pedidoClient.listarPedidosDetalhadosPorAtendimento(8)).thenReturn(List.of(pedido));

        List<PedidoMesaStatusResponse> response =
                mesaPedidoService.listarPedidosDoAtendimentoAtual(123456, criarAuthenticationMesa());

        assertEquals(1, response.size());
        PedidoMesaStatusResponse pedidoResponse = response.getFirst();
        assertEquals(25, pedidoResponse.id());
        assertEquals(100025, pedidoResponse.codigo());
        assertEquals("MESA", pedidoResponse.canal());
        assertEquals("PROBLEMA_COZINHA", pedidoResponse.status());
        assertEquals(1, pedidoResponse.idMesa());
        assertEquals(8, pedidoResponse.idAtendimento());
        assertEquals(123456, pedidoResponse.codigoAtendimento());
        assertEquals(dataCriacao, pedidoResponse.dataCriacao());
        assertEquals(1, pedidoResponse.itens().size());
        assertEquals(10, pedidoResponse.itens().getFirst().idProduto());
        assertEquals(null, pedidoResponse.itens().getFirst().nome());
        assertEquals(2, pedidoResponse.itens().getFirst().quantidade());
        assertEquals("Sem cebola", pedidoResponse.itens().getFirst().observacao());
        verify(mesaClient).validarSessaoMesa(1, 123456);
        verify(pedidoClient).listarPedidosDetalhadosPorAtendimento(8);
    }

    @Test
    void listarPedidosDoAtendimentoAtualDeveMapearFalhaDoMsPedidos() {
        SessaoMesaResponse sessao = new SessaoMesaResponse(1, 8, 123456, 7, "OCUPADA");

        when(mesaClient.validarSessaoMesa(1, 123456)).thenReturn(sessao);
        when(pedidoClient.listarPedidosDetalhadosPorAtendimento(8)).thenThrow(feignException(500));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> mesaPedidoService.listarPedidosDoAtendimentoAtual(123456, criarAuthenticationMesa())
        );

        assertEquals(ErrorEnum.MS_PEDIDOS_INDISPONIVEL, exception.getErrorEnum());
        verify(mesaClient).validarSessaoMesa(1, 123456);
        verify(pedidoClient).listarPedidosDetalhadosPorAtendimento(8);
    }

    private CriarPedidoMesaRequest criarRequest() {
        return new CriarPedidoMesaRequest(
                123456,
                List.of(new ItemPedidoMesaRequest(10, 2, "Sem cebola"))
        );
    }

    private Authentication criarAuthenticationMesa() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                101L,
                "Mesa 1",
                "mesa01@fourkitchen.com",
                "MESA",
                1
        );

        return new UsernamePasswordAuthenticationToken(usuario, null, List.of());
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/mesas/1/sessoes/123456/validar",
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

        return FeignException.errorStatus("validarSessaoMesa", response);
    }
}
