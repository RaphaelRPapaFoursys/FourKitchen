package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.CriarPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoTotemRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.ItemPedidoTotemRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoTotemResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.mapper.ItemPedidoTotemRequestMapper;
import br.com.fourkitchen.bff_restaurante.mapper.PedidoTotemResponseMapper;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TotemPedidoServiceTest {

    @Mock
    private ProdutoClient produtoClient;

    @Mock
    private PedidoClient pedidoClient;

    private final ItemPedidoTotemRequestMapper itemPedidoTotemRequestMapper = new ItemPedidoTotemRequestMapper();

    private final PedidoTotemResponseMapper pedidoTotemResponseMapper = new PedidoTotemResponseMapper();

    private TotemPedidoService totemPedidoService;

    @BeforeEach
    void setUp() {
        totemPedidoService = new TotemPedidoService(
                produtoClient,
                pedidoClient,
                itemPedidoTotemRequestMapper,
                pedidoTotemResponseMapper
        );
    }

    @Test
    void criarPedidoDeveValidarProdutosECriarPedidoComoTotemSemMesa() {
        CriarPedidoTotemRequest request = criarRequest();
        br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse pedidoResponse =
                new br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse(
                        25,
                        100025,
                        "TOTEM",
                        "ENVIADO_COZINHA",
                        null,
                        201,
                        null
        );

        when(produtoClient.verificarDisponibilidade(10))
                .thenReturn(new ProdutoDisponibilidadeResponse(10, true, new BigDecimal("29.90")));
        when(pedidoClient.criarPedido(any(CriarPedidoRequest.class))).thenReturn(pedidoResponse);

        PedidoTotemResponse response = totemPedidoService.criarPedido(request, criarAuthenticationTotem());

        assertEquals(25, response.id());
        assertEquals(100025, response.codigo());
        assertEquals("TOTEM", response.canal());
        assertEquals("ENVIADO_COZINHA", response.status());

        ArgumentCaptor<CriarPedidoRequest> pedidoRequestCaptor = ArgumentCaptor.forClass(CriarPedidoRequest.class);
        verify(pedidoClient).criarPedido(pedidoRequestCaptor.capture());

        CriarPedidoRequest pedidoRequest = pedidoRequestCaptor.getValue();
        assertEquals("TOTEM", pedidoRequest.canal());
        assertEquals("ENVIADO_COZINHA", pedidoRequest.status());
        assertNull(pedidoRequest.idMesa());
        assertEquals(201, pedidoRequest.idUsuario());
        assertNull(pedidoRequest.idAtendimento());
        assertEquals(1, pedidoRequest.itens().size());
        assertEquals(10, pedidoRequest.itens().getFirst().idProduto());
        assertEquals(new BigDecimal("29.90"), pedidoRequest.itens().getFirst().precoUnitario());
        verify(produtoClient).verificarDisponibilidade(10);
    }

    @Test
    void criarPedidoDeveBloquearProdutoIndisponivel() {
        CriarPedidoTotemRequest request = criarRequest();

        when(produtoClient.verificarDisponibilidade(10))
                .thenReturn(new ProdutoDisponibilidadeResponse(10, false, new BigDecimal("29.90")));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> totemPedidoService.criarPedido(request, criarAuthenticationTotem())
        );

        assertEquals(ErrorEnum.PRODUTO_INDISPONIVEL, exception.getErrorEnum());
        verify(produtoClient).verificarDisponibilidade(10);
        verifyNoInteractions(pedidoClient);
    }

    @Test
    void criarPedidoDeveBloquearProdutoNaoEncontrado() {
        CriarPedidoTotemRequest request = criarRequest();

        when(produtoClient.verificarDisponibilidade(10)).thenThrow(feignException(404));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> totemPedidoService.criarPedido(request, criarAuthenticationTotem())
        );

        assertEquals(ErrorEnum.PRODUTO_INDISPONIVEL, exception.getErrorEnum());
        verify(produtoClient).verificarDisponibilidade(10);
        verifyNoInteractions(pedidoClient);
    }

    @Test
    void criarPedidoDeveLancarServicoProdutosIndisponivelQuandoMsProdutosFalhar() {
        CriarPedidoTotemRequest request = criarRequest();

        when(produtoClient.verificarDisponibilidade(10)).thenThrow(feignException(500));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> totemPedidoService.criarPedido(request, criarAuthenticationTotem())
        );

        assertEquals(ErrorEnum.MS_PRODUTOS_INDISPONIVEL, exception.getErrorEnum());
        verify(produtoClient).verificarDisponibilidade(10);
        verifyNoInteractions(pedidoClient);
    }

    @Test
    void criarPedidoDeveLancarServicoPedidosIndisponivelQuandoMsPedidosFalhar() {
        CriarPedidoTotemRequest request = criarRequest();

        when(produtoClient.verificarDisponibilidade(10))
                .thenReturn(new ProdutoDisponibilidadeResponse(10, true, new BigDecimal("29.90")));
        when(pedidoClient.criarPedido(any(CriarPedidoRequest.class))).thenThrow(feignException(500));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> totemPedidoService.criarPedido(request, criarAuthenticationTotem())
        );

        assertEquals(ErrorEnum.MS_PEDIDOS_INDISPONIVEL, exception.getErrorEnum());
        verify(produtoClient).verificarDisponibilidade(10);
        verify(pedidoClient).criarPedido(any(CriarPedidoRequest.class));
    }

    @Test
    void criarPedidoDeveBloquearPerfilDiferenteDeTotem() {
        CriarPedidoTotemRequest request = criarRequest();
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                201L,
                "Garcom",
                "garcom01",
                "GARCOM",
                null
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, List.of());

        BaseException exception = assertThrows(
                BaseException.class,
                () -> totemPedidoService.criarPedido(request, authentication)
        );

        assertEquals(ErrorEnum.ACESSO_NEGADO, exception.getErrorEnum());
        verifyNoInteractions(produtoClient, pedidoClient);
    }

    private CriarPedidoTotemRequest criarRequest() {
        return new CriarPedidoTotemRequest(
                List.of(new ItemPedidoTotemRequest(10, 2, "Sem cebola"))
        );
    }

    private Authentication criarAuthenticationTotem() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                201L,
                "Totem 1",
                "totem01@fourkitchen.com",
                "TOTEM",
                null
        );

        return new UsernamePasswordAuthenticationToken(usuario, null, List.of());
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/produtos/10/disponibilidade",
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

        return FeignException.errorStatus("verificarDisponibilidade", response);
    }
}
