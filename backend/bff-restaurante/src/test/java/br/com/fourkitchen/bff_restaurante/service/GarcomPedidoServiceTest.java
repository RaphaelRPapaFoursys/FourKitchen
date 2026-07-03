package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.SessaoMesaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.CriarPedidoRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.ItemPedidoGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoGarcomResponse;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GarcomPedidoServiceTest {

    @Mock
    private MesaClient mesaClient;

    @Mock
    private PedidoClient pedidoClient;

    @InjectMocks
    private GarcomPedidoService garcomPedidoService;

    @Test
    void criarPedidoDeveValidarMesaDoGarcomECriarPedidoComoGarcom() {
        CriarPedidoGarcomRequest request = criarRequest();
        Authentication authentication = criarAuthentication(7L);
        SessaoMesaResponse sessao = new SessaoMesaResponse(1, 8, 123456, 7, "OCUPADA");
        br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse pedidoResponse =
                new br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse(
                        25,
                        100025,
                        "GARCOM",
                        "ENVIADO_COZINHA",
                        1,
                        7,
                        8
                );

        when(mesaClient.validarMesaAtribuidaGarcom(1, 7)).thenReturn(sessao);
        when(pedidoClient.criarPedido(any(CriarPedidoRequest.class))).thenReturn(pedidoResponse);

        PedidoGarcomResponse response = garcomPedidoService.criarPedido(request, authentication);

        assertEquals(25, response.id());
        assertEquals(100025, response.codigo());
        assertEquals("GARCOM", response.canal());
        assertEquals("ENVIADO_COZINHA", response.status());
        assertEquals(1, response.idMesa());
        assertEquals(7, response.idGarcom());
        assertEquals(8, response.idAtendimento());

        ArgumentCaptor<CriarPedidoRequest> pedidoRequestCaptor = ArgumentCaptor.forClass(CriarPedidoRequest.class);
        verify(pedidoClient).criarPedido(pedidoRequestCaptor.capture());

        CriarPedidoRequest pedidoRequest = pedidoRequestCaptor.getValue();
        assertEquals("GARCOM", pedidoRequest.canal());
        assertEquals("ENVIADO_COZINHA", pedidoRequest.status());
        assertEquals(1, pedidoRequest.idMesa());
        assertEquals(7, pedidoRequest.idUsuario());
        assertEquals(8, pedidoRequest.idAtendimento());
        assertEquals(1, pedidoRequest.itens().size());
        assertEquals("Sem cebola", pedidoRequest.itens().getFirst().observacao());
        verify(mesaClient).validarMesaAtribuidaGarcom(1, 7);
    }

    @Test
    void criarPedidoDeveBloquearMesaDeOutroGarcom() {
        CriarPedidoGarcomRequest request = criarRequest();
        Authentication authentication = criarAuthentication(7L);

        when(mesaClient.validarMesaAtribuidaGarcom(1, 7)).thenThrow(feignException(403));

        BaseException exception = assertThrows(BaseException.class, () -> garcomPedidoService.criarPedido(request, authentication));

        assertEquals(ErrorEnum.MESA_NAO_ATRIBUIDA_AO_GARCOM, exception.getErrorEnum());
        verify(mesaClient).validarMesaAtribuidaGarcom(1, 7);
        verifyNoInteractions(pedidoClient);
    }

    @Test
    void criarPedidoDeveLancarServicoPedidosIndisponivelQuandoMsPedidosFalhar() {
        CriarPedidoGarcomRequest request = criarRequest();
        Authentication authentication = criarAuthentication(7L);
        SessaoMesaResponse sessao = new SessaoMesaResponse(1, 8, 123456, 7, "OCUPADA");

        when(mesaClient.validarMesaAtribuidaGarcom(1, 7)).thenReturn(sessao);
        when(pedidoClient.criarPedido(any(CriarPedidoRequest.class))).thenThrow(feignException(500));

        BaseException exception = assertThrows(BaseException.class, () -> garcomPedidoService.criarPedido(request, authentication));

        assertEquals(ErrorEnum.MS_PEDIDOS_INDISPONIVEL, exception.getErrorEnum());
        verify(mesaClient).validarMesaAtribuidaGarcom(1, 7);
        verify(pedidoClient).criarPedido(any(CriarPedidoRequest.class));
    }

    private CriarPedidoGarcomRequest criarRequest() {
        return new CriarPedidoGarcomRequest(
                1,
                List.of(new ItemPedidoGarcomRequest(10, 2, new BigDecimal("29.90"), "Sem cebola"))
        );
    }

    private Authentication criarAuthentication(Long idGarcom) {
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                idGarcom,
                "Amanda",
                "amanda@fourkitchen.com",
                "GARCOM"
        );

        return new UsernamePasswordAuthenticationToken(usuario, null, List.of());
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/mesas/1/garcons/7/validar",
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

        return FeignException.errorStatus("validarMesaAtribuidaGarcom", response);
    }
}
