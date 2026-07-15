package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoMesaRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.ItemPedidoMesaRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.ItemPedidoMesaStatusResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoMesaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoMesaStatusResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ResumoContaMesaResponse;
import br.com.fourkitchen.bff_restaurante.service.MesaPedidoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MesaPedidoControllerTest {

    @Mock
    private MesaPedidoService mesaPedidoService;

    @InjectMocks
    private MesaPedidoController mesaPedidoController;

    @Test
    void criarPedidoDeveRetornarCreated() {
        CriarPedidoMesaRequest request = new CriarPedidoMesaRequest(
                123456,
                List.of(new ItemPedidoMesaRequest(10, 2, null))
        );
        Authentication authentication = mock(Authentication.class);
        PedidoMesaResponse pedidoResponse = new PedidoMesaResponse(
                25,
                100025,
                "MESA",
                "ENVIADO_COZINHA",
                1,
                8
        );

        when(mesaPedidoService.criarPedido(request, authentication)).thenReturn(pedidoResponse);

        ResponseEntity<PedidoMesaResponse> response = mesaPedidoController.criarPedido(request, authentication);

        assertEquals(201, response.getStatusCode().value());
        assertSame(pedidoResponse, response.getBody());
        verify(mesaPedidoService).criarPedido(request, authentication);
    }

    @Test
    void listarPedidosDoAtendimentoAtualDeveRetornarOk() {
        Authentication authentication = mock(Authentication.class);
        PedidoMesaStatusResponse pedidoResponse = new PedidoMesaStatusResponse(
                25,
                100025,
                "MESA",
                "PROBLEMA_COZINHA",
                1,
                8,
                123456,
                LocalDateTime.of(2026, 7, 2, 10, 30),
                new BigDecimal("59.80"),
                List.of(new ItemPedidoMesaStatusResponse(
                        10,
                        null,
                        2,
                        new BigDecimal("29.90"),
                        new BigDecimal("59.80"),
                        "Sem cebola"
                ))
        );

        when(mesaPedidoService.listarPedidosDoAtendimentoAtual(123456, authentication))
                .thenReturn(List.of(pedidoResponse));

        ResponseEntity<List<PedidoMesaStatusResponse>> response =
                mesaPedidoController.listarPedidosDoAtendimentoAtual(123456, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(pedidoResponse), response.getBody());
        verify(mesaPedidoService).listarPedidosDoAtendimentoAtual(123456, authentication);
    }

    @Test
    void buscarResumoContaAtualDeveRetornarOk() {
        Authentication authentication = mock(Authentication.class);
        ResumoContaMesaResponse resumo = new ResumoContaMesaResponse(
                8,
                123456,
                new BigDecimal("149.70"),
                3,
                7
        );

        when(mesaPedidoService.buscarResumoContaAtual(123456, authentication)).thenReturn(resumo);

        ResponseEntity<ResumoContaMesaResponse> response =
                mesaPedidoController.buscarResumoContaAtual(123456, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertSame(resumo, response.getBody());
        verify(mesaPedidoService).buscarResumoContaAtual(123456, authentication);
    }
}
