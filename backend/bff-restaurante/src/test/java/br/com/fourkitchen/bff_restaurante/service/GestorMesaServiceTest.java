package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.AtribuirGarcomClientRequest;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.HistoricoAtendimentoClientResponse;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaClientResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.usuarios.UsuarioClient;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.AtribuirGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.GarcomResumoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.HistoricoAtendimentoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGestorPaginadaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGestorResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ResumoPainelResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.mapper.GarcomResumoResponseMapper;
import br.com.fourkitchen.bff_restaurante.mapper.MesaGestorMapperSource;
import br.com.fourkitchen.bff_restaurante.mapper.MesaGestorResponseMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorMesaServiceTest {

    private static final String AUTHORIZATION = "Bearer token";

    @Mock
    private MesaClient mesaClient;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient pedidoClient;

    @Mock
    private MesaGestorResponseMapper mesaGestorResponseMapper;

    @Mock
    private GarcomResumoResponseMapper garcomResumoResponseMapper;

    @InjectMocks
    private GestorMesaService gestorMesaService;

    @Test
    void listarMesasDeveBuscarMesasEGarconsEMapearComNomeDoGarcom() {
        MesaClientResponse mesa = criarMesa(1, 10, 7);
        UsuarioClientResponse usuario = criarUsuario(7, "Amanda Souza", "GARCOM", true);
        GarcomResumoResponse garcom = criarGarcom(7, "Amanda Souza");
        MesaGestorResponse response = criarMesaResponse(1, "Amanda Souza");

        when(mesaClient.listarMesas()).thenReturn(List.of(mesa));
        when(usuarioClient.listarUsuariosAtivos(AUTHORIZATION)).thenReturn(List.of(usuario));
        when(garcomResumoResponseMapper.map(usuario)).thenReturn(garcom);
        when(mesaGestorResponseMapper.map(any(MesaGestorMapperSource.class))).thenReturn(response);

        List<MesaGestorResponse> resultado = gestorMesaService.listarMesas(AUTHORIZATION);

        assertEquals(List.of(response), resultado);
        verify(mesaClient).listarMesas();
        verify(usuarioClient).listarUsuariosAtivos(AUTHORIZATION);
        verify(garcomResumoResponseMapper).map(usuario);

        ArgumentCaptor<MesaGestorMapperSource> sourceCaptor = ArgumentCaptor.forClass(MesaGestorMapperSource.class);
        verify(mesaGestorResponseMapper).map(sourceCaptor.capture());
        assertSame(mesa, sourceCaptor.getValue().mesa());
        assertEquals("Amanda Souza", sourceCaptor.getValue().garcomNome());
    }

    @Test
    void listarMesasDeveNaoBuscarGarconsQuandoMesasNaoTiveremGarcomAtribuido() {
        MesaClientResponse mesa = criarMesa(1, 10, null);
        MesaGestorResponse response = criarMesaResponse(1, null);

        when(mesaClient.listarMesas()).thenReturn(List.of(mesa));
        when(mesaGestorResponseMapper.map(any(MesaGestorMapperSource.class))).thenReturn(response);

        List<MesaGestorResponse> resultado = gestorMesaService.listarMesas(AUTHORIZATION);

        assertEquals(List.of(response), resultado);
        verify(mesaClient).listarMesas();
        verifyNoInteractions(usuarioClient, garcomResumoResponseMapper);
    }

    @Test
    void listarMesasPaginadasDeveFiltrarOrdenarPaginarEmMemoria() {
        MesaClientResponse mesa = criarMesa(1, 10, 7);
        UsuarioClientResponse usuario = criarUsuario(7, "Amanda Souza", "GARCOM", true);
        GarcomResumoResponse garcom = criarGarcom(7, "Amanda Souza");
        MesaGestorResponse mesaResponse = criarMesaResponse(1, "Amanda Souza");

        when(mesaClient.listarMesas()).thenReturn(List.of(mesa));
        when(usuarioClient.listarUsuariosAtivos(AUTHORIZATION)).thenReturn(List.of(usuario));
        when(garcomResumoResponseMapper.map(usuario)).thenReturn(garcom);
        when(mesaGestorResponseMapper.map(any(MesaGestorMapperSource.class))).thenReturn(mesaResponse);

        MesaGestorPaginadaResponse resultado = gestorMesaService.listarMesasPaginadas(
                AUTHORIZATION,
                0,
                10,
                "numero,asc",
                null,
                7,
                "Amanda"
        );

        assertEquals(List.of(mesaResponse), resultado.content());
        assertEquals(0, resultado.page());
        assertEquals(10, resultado.size());
        assertEquals(1L, resultado.totalElements());
        assertEquals(1, resultado.totalPages());
        assertEquals(true, resultado.first());
        assertEquals(true, resultado.last());
        verify(mesaClient).listarMesas();
        verify(usuarioClient).listarUsuariosAtivos(AUTHORIZATION);
    }

    @Test
    void buscarResumoPainelDeveCalcularKpisECargaDosGarcons() {
        MesaClientResponse mesa = criarMesaComAtendimento(1, 10, 7, 100);
        UsuarioClientResponse usuario = criarUsuario(7, "Amanda Souza", "GARCOM", true);
        GarcomResumoResponse garcom = criarGarcom(7, "Amanda Souza");
        PedidoCozinhaResponse pedido = criarPedido(1, 100, "PRONTO", 2, BigDecimal.TEN);

        when(mesaClient.listarMesas()).thenReturn(List.of(mesa));
        when(usuarioClient.listarUsuariosAtivos(AUTHORIZATION)).thenReturn(List.of(usuario));
        when(garcomResumoResponseMapper.map(usuario)).thenReturn(garcom);
        when(pedidoClient.listarPedidosAtivosDetalhadosPorAtendimentos(List.of(100))).thenReturn(List.of(pedido));

        ResumoPainelResponse resultado = gestorMesaService.buscarResumoPainel(AUTHORIZATION);

        assertEquals(0, resultado.mesasLivres());
        assertEquals(0, resultado.emPreparo());
        assertEquals(1, resultado.prontos());
        assertEquals(0, resultado.problemas());
        assertEquals(new BigDecimal("20.00"), resultado.ticketMedio());
        assertEquals(1, resultado.cargaGarcons().getFirst().mesasAtivas());
    }

    @Test
    void listarMesasPaginadasEBuscarResumoPainelDevemReutilizarSnapshotDentroDoTtl() {
        MesaClientResponse mesa = criarMesaComAtendimento(1, 10, 7, 100);
        UsuarioClientResponse usuario = criarUsuario(7, "Amanda Souza", "GARCOM", true);
        GarcomResumoResponse garcom = criarGarcom(7, "Amanda Souza");
        PedidoCozinhaResponse pedido = criarPedido(1, 100, "EM_PREPARO", 1, BigDecimal.TEN);
        MesaGestorResponse mesaResponse = criarMesaResponse(1, "Amanda Souza");

        when(mesaClient.listarMesas()).thenReturn(List.of(mesa));
        when(usuarioClient.listarUsuariosAtivos(AUTHORIZATION)).thenReturn(List.of(usuario));
        when(garcomResumoResponseMapper.map(usuario)).thenReturn(garcom);
        when(pedidoClient.listarPedidosAtivosDetalhadosPorAtendimentos(List.of(100))).thenReturn(List.of(pedido));
        when(mesaGestorResponseMapper.map(any(MesaGestorMapperSource.class))).thenReturn(mesaResponse);

        gestorMesaService.listarMesasPaginadas(AUTHORIZATION, 0, 10, "criticidade", null, null, null);
        ResumoPainelResponse resumo = gestorMesaService.buscarResumoPainel(AUTHORIZATION);

        assertEquals(1, resumo.emPreparo());
        verify(mesaClient, times(1)).listarMesas();
        verify(usuarioClient, times(1)).listarUsuariosAtivos(AUTHORIZATION);
        verify(pedidoClient, times(1)).listarPedidosAtivosDetalhadosPorAtendimentos(List.of(100));
    }

    @Test
    void abrirMesaDeveInvalidarSnapshotDoPainel() {
        MesaClientResponse mesa = criarMesa(1, 10, null);
        MesaGestorResponse mesaResponse = criarMesaResponse(1, null);

        when(mesaClient.listarMesas()).thenReturn(List.of(mesa));
        when(usuarioClient.listarUsuariosAtivos(AUTHORIZATION)).thenReturn(List.of());
        when(mesaGestorResponseMapper.map(any(MesaGestorMapperSource.class))).thenReturn(mesaResponse);
        when(mesaClient.abrirMesa(1)).thenReturn(mesa);

        gestorMesaService.listarMesasPaginadas(AUTHORIZATION, 0, 10, "criticidade", null, null, null);
        gestorMesaService.abrirMesa(1, AUTHORIZATION);
        gestorMesaService.buscarResumoPainel(AUTHORIZATION);

        verify(mesaClient, times(2)).listarMesas();
        verify(usuarioClient, times(2)).listarUsuariosAtivos(AUTHORIZATION);
    }

    @Test
    void listarGarconsDeveRetornarSomenteUsuariosAtivosComPerfilGarcom() {
        UsuarioClientResponse garcomUsuario = criarUsuario(7, "Amanda Souza", "GARCOM", true);
        UsuarioClientResponse cozinheiroUsuario = criarUsuario(8, "Carlos Lima", "COZINHA", true);
        UsuarioClientResponse garcomInativo = criarUsuario(9, "Bruno Silva", "GARCOM", false);
        GarcomResumoResponse garcom = criarGarcom(7, "Amanda Souza");

        when(usuarioClient.listarUsuariosAtivos(AUTHORIZATION)).thenReturn(List.of(
                cozinheiroUsuario,
                garcomInativo,
                garcomUsuario
        ));
        when(garcomResumoResponseMapper.map(garcomUsuario)).thenReturn(garcom);

        List<GarcomResumoResponse> resultado = gestorMesaService.listarGarcons(AUTHORIZATION);

        assertEquals(List.of(garcom), resultado);
        verify(usuarioClient).listarUsuariosAtivos(AUTHORIZATION);
        verify(garcomResumoResponseMapper).map(garcomUsuario);
    }

    @Test
    void listarHistoricoAtendimentosDeveBuscarHistoricoEEnriquecerNomeDoGarcom() {
        HistoricoAtendimentoClientResponse historico = new HistoricoAtendimentoClientResponse(
                1,
                8,
                123456,
                1,
                10,
                7,
                null,
                new BigDecimal("149.70"),
                3,
                7,
                LocalDateTime.of(2026, 7, 2, 10, 0),
                LocalDateTime.of(2026, 7, 2, 11, 20),
                80
        );
        UsuarioClientResponse usuario = criarUsuario(7, "Amanda Souza", "GARCOM", true);
        GarcomResumoResponse garcom = criarGarcom(7, "Amanda Souza");

        when(mesaClient.listarHistoricoAtendimentos()).thenReturn(List.of(historico));
        when(usuarioClient.listarUsuariosAtivos(AUTHORIZATION)).thenReturn(List.of(usuario));
        when(garcomResumoResponseMapper.map(usuario)).thenReturn(garcom);

        List<HistoricoAtendimentoResponse> resultado = gestorMesaService.listarHistoricoAtendimentos(AUTHORIZATION);

        assertEquals(1, resultado.size());
        assertEquals(8, resultado.getFirst().idAtendimento());
        assertEquals(10, resultado.getFirst().numeroMesa());
        assertEquals(7, resultado.getFirst().idGarcom());
        assertEquals("Amanda Souza", resultado.getFirst().nomeGarcom());
        assertEquals(new BigDecimal("149.70"), resultado.getFirst().valorFinal());
        assertEquals(80, resultado.getFirst().duracaoMinutos());
        verify(mesaClient).listarHistoricoAtendimentos();
        verify(usuarioClient).listarUsuariosAtivos(AUTHORIZATION);
        verify(garcomResumoResponseMapper).map(usuario);
    }

    @Test
    void abrirMesaDeveDelegarParaMsMesasEMapearResposta() {
        MesaClientResponse mesa = criarMesa(1, 10, null);
        MesaGestorResponse response = criarMesaResponse(1, null);

        when(mesaClient.abrirMesa(1)).thenReturn(mesa);
        when(mesaGestorResponseMapper.map(any(MesaGestorMapperSource.class))).thenReturn(response);

        MesaGestorResponse resultado = gestorMesaService.abrirMesa(1, AUTHORIZATION);

        assertSame(response, resultado);
        verify(mesaClient).abrirMesa(1);
        verifyNoInteractions(usuarioClient, garcomResumoResponseMapper);
    }

    @Test
    void fecharMesaDeveMapearMesaNaoEncontrada() {
        when(mesaClient.fecharMesa(99)).thenThrow(feignException(404));

        BaseException exception = assertThrows(BaseException.class, () -> gestorMesaService.fecharMesa(99, AUTHORIZATION));

        assertEquals(ErrorEnum.MESA_NAO_ENCONTRADA, exception.getErrorEnum());
        verify(mesaClient).fecharMesa(99);
        verifyNoInteractions(usuarioClient, mesaGestorResponseMapper, garcomResumoResponseMapper);
    }

    @Test
    void fecharMesaDeveMapearRegraDeNegocioInvalida() {
        when(mesaClient.fecharMesa(1)).thenThrow(feignException(400));

        BaseException exception = assertThrows(BaseException.class, () -> gestorMesaService.fecharMesa(1, AUTHORIZATION));

        assertEquals(ErrorEnum.DADOS_INVALIDOS, exception.getErrorEnum());
        verify(mesaClient).fecharMesa(1);
    }

    @Test
    void atribuirGarcomDeveValidarGarcomEDelegarParaMsMesas() {
        UsuarioClientResponse usuario = criarUsuario(7, "Amanda Souza", "GARCOM", true);
        GarcomResumoResponse garcom = criarGarcom(7, "Amanda Souza");
        MesaClientResponse mesa = criarMesa(1, 10, 7);
        MesaGestorResponse response = criarMesaResponse(1, "Amanda Souza");

        when(usuarioClient.listarUsuariosAtivos(AUTHORIZATION)).thenReturn(List.of(usuario));
        when(garcomResumoResponseMapper.map(usuario)).thenReturn(garcom);
        when(mesaClient.atribuirGarcom(eq(1), any(AtribuirGarcomClientRequest.class))).thenReturn(mesa);
        when(mesaGestorResponseMapper.map(any(MesaGestorMapperSource.class))).thenReturn(response);

        MesaGestorResponse resultado = gestorMesaService.atribuirGarcom(
                1,
                new AtribuirGarcomRequest(7),
                AUTHORIZATION
        );

        assertSame(response, resultado);
        verify(usuarioClient).listarUsuariosAtivos(AUTHORIZATION);

        ArgumentCaptor<AtribuirGarcomClientRequest> requestCaptor =
                ArgumentCaptor.forClass(AtribuirGarcomClientRequest.class);
        verify(mesaClient).atribuirGarcom(eq(1), requestCaptor.capture());
        assertEquals(7, requestCaptor.getValue().garcomId());
    }

    @Test
    void atribuirGarcomDeveBloquearUsuarioSemPerfilGarcom() {
        UsuarioClientResponse usuario = criarUsuario(8, "Carlos Lima", "COZINHA", true);

        when(usuarioClient.listarUsuariosAtivos(AUTHORIZATION)).thenReturn(List.of(usuario));

        BaseException exception = assertThrows(BaseException.class, () -> gestorMesaService.atribuirGarcom(
                1,
                new AtribuirGarcomRequest(8),
                AUTHORIZATION
        ));

        assertEquals(ErrorEnum.GARCOM_INVALIDO, exception.getErrorEnum());
        verify(usuarioClient).listarUsuariosAtivos(AUTHORIZATION);
        verifyNoInteractions(mesaClient, mesaGestorResponseMapper);
    }

    @Test
    void listarGarconsDeveMapearTokenInvalidoDoMsUsuarios() {
        when(usuarioClient.listarUsuariosAtivos(AUTHORIZATION)).thenThrow(feignException(401));

        BaseException exception = assertThrows(BaseException.class, () -> gestorMesaService.listarGarcons(AUTHORIZATION));

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
        verify(usuarioClient).listarUsuariosAtivos(AUTHORIZATION);
    }

    @Test
    void listarGarconsDeveLancarTokenInvalidoQuandoAuthorizationNaoForBearer() {
        BaseException exception = assertThrows(BaseException.class, () -> gestorMesaService.listarGarcons("token"));

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
        verifyNoInteractions(usuarioClient, mesaClient, mesaGestorResponseMapper, garcomResumoResponseMapper);
    }

    private MesaClientResponse criarMesa(Integer id, Integer numero, Integer garcomId) {
        return new MesaClientResponse(
                id,
                numero,
                "OCUPADA",
                garcomId,
                123456,
                LocalDateTime.of(2026, 7, 2, 10, 0),
                null,
                null
        );
    }

    private MesaClientResponse criarMesaComAtendimento(Integer id, Integer numero, Integer garcomId, Integer idAtendimento) {
        return new MesaClientResponse(
                id,
                numero,
                "OCUPADA",
                garcomId,
                123456,
                LocalDateTime.of(2026, 7, 2, 10, 0),
                null,
                idAtendimento
        );
    }

    private PedidoCozinhaResponse criarPedido(
            Integer id,
            Integer idAtendimento,
            String status,
            Integer quantidade,
            BigDecimal precoUnitario
    ) {
        return new PedidoCozinhaResponse(
                id,
                10,
                "MESA",
                status,
                1,
                idAtendimento,
                LocalDateTime.now().minusMinutes(3),
                null,
                null,
                List.of(new ItemPedidoCozinhaResponse(1, 1, null, quantidade, precoUnitario, null))
        );
    }

    private UsuarioClientResponse criarUsuario(Integer id, String nome, String perfil, Boolean ativo) {
        return new UsuarioClientResponse(
                id,
                nome,
                nome.toLowerCase().replace(" ", ".") + "@fourkitchen.com",
                perfil,
                null,
                ativo
        );
    }

    private GarcomResumoResponse criarGarcom(Integer id, String nome) {
        return new GarcomResumoResponse(
                id,
                nome,
                nome.toLowerCase().replace(" ", ".") + "@fourkitchen.com"
        );
    }

    private MesaGestorResponse criarMesaResponse(Integer id, String garcomNome) {
        return new MesaGestorResponse(
                id,
                10,
                "OCUPADA",
                garcomNome == null ? null : 7,
                garcomNome,
                123456,
                LocalDateTime.of(2026, 7, 2, 10, 0),
                null,
                List.of()
        );
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/gestor/mesas",
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

        return FeignException.errorStatus("gestorMesa", response);
    }
}
