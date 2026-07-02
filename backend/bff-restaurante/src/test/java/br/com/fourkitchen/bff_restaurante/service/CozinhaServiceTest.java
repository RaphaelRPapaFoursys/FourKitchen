package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoFilaCozinhaResponse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CozinhaServiceTest {

    @Mock
    private PedidoClient pedidoClient;

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
