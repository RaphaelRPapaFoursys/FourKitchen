package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.AtribuirGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.GarcomResumoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.HistoricoAtendimentoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGestorPaginadaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGestorResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoDetalheGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ResumoPainelResponse;
import br.com.fourkitchen.bff_restaurante.service.GestorMesaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorMesaControllerTest {

    private static final String AUTHORIZATION = "Bearer token";

    @Mock
    private GestorMesaService gestorMesaService;

    @InjectMocks
    private GestorMesaController gestorMesaController;

    @Test
    void listarMesasDeveRetornarOk() {
        MesaGestorResponse mesa = criarMesa();

        when(gestorMesaService.listarMesas(AUTHORIZATION)).thenReturn(List.of(mesa));

        ResponseEntity<List<MesaGestorResponse>> response = gestorMesaController.listarMesas(AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(mesa, response.getBody().getFirst());
        verify(gestorMesaService).listarMesas(AUTHORIZATION);
    }

    @Test
    void listarPedidosDetalhadosDeveRetornarOk() {
        when(gestorMesaService.listarPedidosDetalhados(1)).thenReturn(List.of());

        ResponseEntity<List<PedidoDetalheGarcomResponse>> response = gestorMesaController.listarPedidosDetalhados(1);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(), response.getBody());
        verify(gestorMesaService).listarPedidosDetalhados(1);
    }

    @Test
    void listarPedidosDetalhadosPorAtendimentoDeveRetornarOk() {
        when(gestorMesaService.listarPedidosDetalhadosPorAtendimento(80)).thenReturn(List.of());

        ResponseEntity<List<PedidoDetalheGarcomResponse>> response =
                gestorMesaController.listarPedidosDetalhadosPorAtendimento(80);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(), response.getBody());
        verify(gestorMesaService).listarPedidosDetalhadosPorAtendimento(80);
    }

    @Test
    void listarMesasPaginadasDeveRetornarOk() {
        MesaGestorResponse mesa = criarMesa();
        MesaGestorPaginadaResponse pagina = new MesaGestorPaginadaResponse(
                List.of(mesa),
                0,
                10,
                1L,
                1,
                true,
                true
        );

        when(gestorMesaService.listarMesasPaginadas(
                AUTHORIZATION,
                0,
                10,
                "numero,asc",
                "PRONTOS",
                7,
                "Amanda"
        )).thenReturn(pagina);

        ResponseEntity<MesaGestorPaginadaResponse> response = gestorMesaController.listarMesasPaginadas(
                0,
                10,
                "numero,asc",
                "PRONTOS",
                7,
                "Amanda",
                AUTHORIZATION
        );

        assertEquals(200, response.getStatusCode().value());
        assertSame(pagina, response.getBody());
        verify(gestorMesaService).listarMesasPaginadas(
                AUTHORIZATION,
                0,
                10,
                "numero,asc",
                "PRONTOS",
                7,
                "Amanda"
        );
    }

    @Test
    void buscarResumoPainelDeveRetornarOk() {
        ResumoPainelResponse resumo = new ResumoPainelResponse(1, 2, 3, 4, 5, null, List.of());

        when(gestorMesaService.buscarResumoPainel(AUTHORIZATION)).thenReturn(resumo);

        ResponseEntity<ResumoPainelResponse> response = gestorMesaController.buscarResumoPainel(AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(resumo, response.getBody());
        verify(gestorMesaService).buscarResumoPainel(AUTHORIZATION);
    }

    @Test
    void listarGarconsDeveRetornarOk() {
        GarcomResumoResponse garcom = new GarcomResumoResponse(
                7,
                "Amanda Souza",
                "amanda@fourkitchen.com"
        );

        when(gestorMesaService.listarGarcons(AUTHORIZATION)).thenReturn(List.of(garcom));

        ResponseEntity<List<GarcomResumoResponse>> response = gestorMesaController.listarGarcons(AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(garcom, response.getBody().getFirst());
        verify(gestorMesaService).listarGarcons(AUTHORIZATION);
    }

    @Test
    void listarHistoricoAtendimentosDeveRetornarOk() {
        HistoricoAtendimentoResponse historico = new HistoricoAtendimentoResponse(
                1,
                8,
                123456,
                1,
                10,
                7,
                "Amanda Souza",
                new BigDecimal("149.70"),
                3,
                7,
                LocalDateTime.of(2026, 7, 2, 10, 0),
                LocalDateTime.of(2026, 7, 2, 11, 20),
                80
        );

        when(gestorMesaService.listarHistoricoAtendimentos(AUTHORIZATION)).thenReturn(List.of(historico));

        ResponseEntity<List<HistoricoAtendimentoResponse>> response =
                gestorMesaController.listarHistoricoAtendimentos(AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(historico, response.getBody().getFirst());
        verify(gestorMesaService).listarHistoricoAtendimentos(AUTHORIZATION);
    }

    @Test
    void abrirMesaDeveRetornarOk() {
        MesaGestorResponse mesa = criarMesa();

        when(gestorMesaService.abrirMesa(1, AUTHORIZATION)).thenReturn(mesa);

        ResponseEntity<MesaGestorResponse> response = gestorMesaController.abrirMesa(1, AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(mesa, response.getBody());
        verify(gestorMesaService).abrirMesa(1, AUTHORIZATION);
    }

    @Test
    void fecharMesaDeveRetornarOk() {
        MesaGestorResponse mesa = criarMesa();

        when(gestorMesaService.fecharMesa(1, AUTHORIZATION)).thenReturn(mesa);

        ResponseEntity<MesaGestorResponse> response = gestorMesaController.fecharMesa(1, AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(mesa, response.getBody());
        verify(gestorMesaService).fecharMesa(1, AUTHORIZATION);
    }

    @Test
    void atribuirGarcomDeveRetornarOk() {
        AtribuirGarcomRequest request = new AtribuirGarcomRequest(7);
        MesaGestorResponse mesa = criarMesa();

        when(gestorMesaService.atribuirGarcom(1, request, AUTHORIZATION)).thenReturn(mesa);

        ResponseEntity<MesaGestorResponse> response = gestorMesaController.atribuirGarcom(
                1,
                request,
                AUTHORIZATION
        );

        assertEquals(200, response.getStatusCode().value());
        assertSame(mesa, response.getBody());
        verify(gestorMesaService).atribuirGarcom(1, request, AUTHORIZATION);
    }

    private MesaGestorResponse criarMesa() {
        return new MesaGestorResponse(
                1,
                10,
                "OCUPADA",
                7,
                "Amanda Souza",
                123456,
                LocalDateTime.of(2026, 7, 2, 10, 0),
                null,
                List.of()
        );
    }
}
