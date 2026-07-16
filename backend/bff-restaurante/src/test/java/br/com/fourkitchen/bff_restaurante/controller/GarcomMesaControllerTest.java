package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.ContaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.FechamentoContaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaDetalheGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGarcomDetalheResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoDetalheGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaProblemasGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.DecisaoProblemaRequest;
import br.com.fourkitchen.bff_restaurante.enums.StatusProdutoPedido;
import br.com.fourkitchen.bff_restaurante.service.GarcomMesaService;
import br.com.fourkitchen.bff_restaurante.service.GarcomProblemaService;
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
class GarcomMesaControllerTest {

    @Mock
    private GarcomMesaService garcomMesaService;

    @Mock
    private GarcomProblemaService garcomProblemaService;

    @InjectMocks
    private GarcomMesaController garcomMesaController;

    @Test
    void listarMesasDeveRetornarOk() {
        Authentication authentication = mock(Authentication.class);
        MesaGarcomResponse mesaResponse = new MesaGarcomResponse(
                1,
                10,
                "OCUPADA",
                8,
                123456,
                7,
                LocalDateTime.of(2026, 7, 2, 10, 0),
                List.of(),
                List.of(),
                false
        );

        when(garcomMesaService.listarMesas(authentication)).thenReturn(List.of(mesaResponse));

        ResponseEntity<List<MesaGarcomResponse>> response = garcomMesaController.listarMesas(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertSame(mesaResponse, response.getBody().getFirst());
        verify(garcomMesaService).listarMesas(authentication);
    }

    @Test
    void detalharMesaDeveRetornarOk() {
        Authentication authentication = mock(Authentication.class);
        MesaGarcomDetalheResponse detalhe = new MesaGarcomDetalheResponse(
                new MesaDetalheGarcomResponse(1, 10, "OCUPADA", 8, 123456, LocalDateTime.of(2026, 7, 2, 10, 0)),
                new ContaGarcomResponse(new BigDecimal("149.70"), new BigDecimal("149.70"), 3, 7),
                List.of(),
                List.of()
        );

        when(garcomMesaService.detalharMesa(1, authentication)).thenReturn(detalhe);

        ResponseEntity<MesaGarcomDetalheResponse> response = garcomMesaController.detalharMesa(1, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertSame(detalhe, response.getBody());
        verify(garcomMesaService).detalharMesa(1, authentication);
    }

    @Test
    void listarPedidosDaMesaDeveRetornarOk() {
        Authentication authentication = mock(Authentication.class);
        PedidoDetalheGarcomResponse pedido = new PedidoDetalheGarcomResponse(
                25,
                100025,
                "GARCOM",
                "EM_PREPARO",
                LocalDateTime.of(2026, 7, 2, 10, 30),
                LocalDateTime.of(2026, 7, 2, 10, 36),
                null,
                List.of()
        );

        when(garcomMesaService.listarPedidosDaMesa(1, authentication)).thenReturn(List.of(pedido));

        ResponseEntity<List<PedidoDetalheGarcomResponse>> response =
                garcomMesaController.listarPedidosDaMesa(1, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertSame(pedido, response.getBody().getFirst());
        verify(garcomMesaService).listarPedidosDaMesa(1, authentication);
    }

    @Test
    void listarProblemasDaMesaDeveRetornarOk() {
        Authentication authentication = mock(Authentication.class);
        MesaProblemasGarcomResponse problemas = new MesaProblemasGarcomResponse(
                new MesaDetalheGarcomResponse(1, 10, "OCUPADA", 8, 123456, LocalDateTime.of(2026, 7, 2, 10, 0)),
                List.of(),
                List.of()
        );
        when(garcomMesaService.listarProblemasDaMesa(1, authentication)).thenReturn(problemas);

        ResponseEntity<MesaProblemasGarcomResponse> response =
                garcomMesaController.listarProblemasDaMesa(1, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertSame(problemas, response.getBody());
        verify(garcomMesaService).listarProblemasDaMesa(1, authentication);
    }

    @Test
    void fecharContaDeveRetornarOk() {
        Authentication authentication = mock(Authentication.class);
        FechamentoContaGarcomResponse fechamento = new FechamentoContaGarcomResponse(
                1,
                10,
                "DISPONIVEL",
                8,
                123456,
                LocalDateTime.of(2026, 7, 2, 10, 0),
                LocalDateTime.of(2026, 7, 2, 11, 20),
                new ContaGarcomResponse(new BigDecimal("149.70"), new BigDecimal("149.70"), 3, 7)
        );

        when(garcomMesaService.fecharConta(1, authentication)).thenReturn(fechamento);

        ResponseEntity<FechamentoContaGarcomResponse> response =
                garcomMesaController.fecharConta(1, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertSame(fechamento, response.getBody());
        verify(garcomMesaService).fecharConta(1, authentication);
    }

    @Test
    void marcarPedidoComoEntregueDeveRetornarSemConteudo() {
        Authentication authentication = mock(Authentication.class);

        ResponseEntity<Void> response = garcomMesaController.marcarPedidoComoEntregue(1, 25, authentication);

        assertEquals(204, response.getStatusCode().value());
        verify(garcomMesaService).marcarPedidoComoEntregue(1, 25, authentication);
    }

    @Test
    void cancelarPedidoAntesDoPreparoDeveRetornarSemConteudo() {
        Authentication authentication = mock(Authentication.class);

        ResponseEntity<Void> response = garcomMesaController.cancelarPedidoAntesDoPreparo(1, 25, authentication);

        assertEquals(204, response.getStatusCode().value());
        verify(garcomMesaService).cancelarPedidoAntesDoPreparo(1, 25, authentication);
    }

    @Test
    void registrarDecisaoProblemaDeveRetornarSemConteudo() {
        Authentication authentication = mock(Authentication.class);
        DecisaoProblemaRequest request = new DecisaoProblemaRequest(
                25,
                80,
                StatusProdutoPedido.DISPONIVEL,
                false,
                12
        );

        ResponseEntity<Void> response = garcomMesaController.registrarDecisaoProblema(
                1,
                request,
                authentication
        );

        assertEquals(204, response.getStatusCode().value());
        verify(garcomProblemaService).registrarDecisao(1, request, authentication);
    }
}
