package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoRetiradaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoBalcaoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoPainelRetiradaResponse;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetiradaServiceTest {

    @Mock
    private PedidoClient pedidoClient;

    @InjectMocks
    private RetiradaService retiradaService;

    @Test
    void deveListarPainelSemExporIdInterno() {
        when(pedidoClient.listarFilaRetiradaTotem()).thenReturn(List.of(criarPedidoRetirada()));

        List<PedidoPainelRetiradaResponse> resultado = retiradaService.listarPainelPublico();

        assertEquals(List.of(new PedidoPainelRetiradaResponse(100025, "PRONTO")), resultado);
    }

    @Test
    void deveListarFilaCompletaParaBalcao() {
        PedidoRetiradaResponse pedido = criarPedidoRetirada();
        when(pedidoClient.listarFilaRetiradaTotem()).thenReturn(List.of(pedido));

        List<PedidoBalcaoResponse> resultado = retiradaService.listarFilaBalcao();

        assertEquals(25, resultado.getFirst().id());
        assertEquals(pedido.dataPronto(), resultado.getFirst().dataPronto());
    }

    @Test
    void deveEntregarPedidoTotemPronto() {
        PedidoResponse response = new PedidoResponse(25, 100025, "TOTEM", "ENTREGUE", null, 7, null);
        when(pedidoClient.entregarPedidoTotem(25)).thenReturn(response);

        PedidoBalcaoResponse resultado = retiradaService.entregar(25);

        assertEquals("ENTREGUE", resultado.status());
        verify(pedidoClient).entregarPedidoTotem(25);
    }

    @Test
    void deveMapearTransicaoInvalida() {
        when(pedidoClient.entregarPedidoTotem(25)).thenThrow(feignException(400));

        BaseException exception = assertThrows(BaseException.class, () -> retiradaService.entregar(25));

        assertEquals(ErrorEnum.TRANSICAO_STATUS_INVALIDA, exception.getErrorEnum());
    }

    private PedidoRetiradaResponse criarPedidoRetirada() {
        return new PedidoRetiradaResponse(
                25,
                100025,
                "PRONTO",
                LocalDateTime.of(2026, 7, 21, 10, 0),
                LocalDateTime.of(2026, 7, 21, 10, 2),
                LocalDateTime.of(2026, 7, 21, 10, 10)
        );
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.PATCH,
                "/api/pedidos/totem/25/entregar",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null
        );
        Response response = Response.builder()
                .status(status)
                .reason("erro")
                .request(request)
                .headers(Map.of())
                .build();
        return FeignException.errorStatus("entregarPedidoTotem", response);
    }
}
