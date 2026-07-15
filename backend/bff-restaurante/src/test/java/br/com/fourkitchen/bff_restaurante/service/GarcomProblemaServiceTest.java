package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.SessaoMesaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.DecisaoProblemaRequest;
import br.com.fourkitchen.bff_restaurante.enums.StatusProdutoPedido;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GarcomProblemaServiceTest {

    @Mock
    private MesaClient mesaClient;

    @Mock
    private PedidoClient pedidoClient;

    @Mock
    private DecisaoProblemaService decisaoProblemaService;

    @InjectMocks
    private GarcomProblemaService garcomProblemaService;

    @Test
    void registrarDecisaoDeveValidarMesaPedidoEItemDoGarcom() {
        Authentication authentication = criarAuthentication();
        DecisaoProblemaRequest request = criarRequest();
        when(mesaClient.validarMesaAtribuidaGarcom(1, 7)).thenReturn(criarSessao());
        when(pedidoClient.listarPedidosDetalhadosPorAtendimento(8)).thenReturn(List.of(criarPedido(25)));

        garcomProblemaService.registrarDecisao(1, request, authentication);

        verify(decisaoProblemaService).registrar(request);
    }

    @Test
    void registrarDecisaoDeveRecusarPedidoDeOutroAtendimento() {
        Authentication authentication = criarAuthentication();
        DecisaoProblemaRequest request = criarRequest();
        when(mesaClient.validarMesaAtribuidaGarcom(1, 7)).thenReturn(criarSessao());
        when(pedidoClient.listarPedidosDetalhadosPorAtendimento(8)).thenReturn(List.of(criarPedido(99)));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> garcomProblemaService.registrarDecisao(1, request, authentication)
        );

        assertEquals(ErrorEnum.PEDIDO_NAO_PERMITE_DECISAO, exception.getErrorEnum());
        verify(decisaoProblemaService, never()).registrar(request);
    }

    private Authentication criarAuthentication() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                7L,
                "Amanda",
                "amanda@fourkitchen.com",
                "GARCOM",
                null
        );
        return new UsernamePasswordAuthenticationToken(usuario, null, List.of());
    }

    private SessaoMesaResponse criarSessao() {
        return new SessaoMesaResponse(1, 8, 123456, 7, "OCUPADA");
    }

    private DecisaoProblemaRequest criarRequest() {
        return new DecisaoProblemaRequest(
                25,
                80,
                StatusProdutoPedido.REMOVIDO,
                false,
                null
        );
    }

    private PedidoCozinhaResponse criarPedido(Integer idPedido) {
        ItemPedidoCozinhaResponse item = new ItemPedidoCozinhaResponse(
                80,
                4,
                "Hamburguer",
                1,
                new BigDecimal("25.00"),
                null,
                "FALTA_PRODUTO"
        );
        return new PedidoCozinhaResponse(
                idPedido,
                100025,
                "MESA",
                "AGUARDANDO_DECISAO",
                1,
                10,
                8,
                LocalDateTime.of(2026, 7, 2, 10, 30),
                null,
                null,
                List.of(item)
        );
    }
}
