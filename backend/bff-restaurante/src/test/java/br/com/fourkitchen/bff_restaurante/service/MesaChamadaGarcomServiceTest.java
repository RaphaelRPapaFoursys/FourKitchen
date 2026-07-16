package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.SessaoMesaResponse;
import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.TipoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.request.ChamarGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.realtime.RealtimeNotifier;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MesaChamadaGarcomServiceTest {

    @Mock
    private MesaClient mesaClient;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private RealtimeNotifier realtimeNotifier;

    @InjectMocks
    private MesaChamadaGarcomService mesaChamadaGarcomService;

    @Test
    void chamarGarcomDeveValidarSessaoECriarNotificacaoParaGarcomResponsavel() {
        ChamarGarcomRequest request = new ChamarGarcomRequest(123456);
        SessaoMesaResponse sessao = new SessaoMesaResponse(1, 8, 123456, 7, "OCUPADA");
        NotificacaoResponse notificacao = criarNotificacaoResponse();

        when(mesaClient.validarSessaoMesa(1, 123456)).thenReturn(sessao);
        when(notificacaoService.criarNotificacao(new CriarNotificacaoRequest(
                TipoNotificacao.CHAMADA_GARCOM,
                DestinoNotificacao.GARCOM,
                1,
                8,
                7
        ))).thenReturn(notificacao);

        NotificacaoResponse response = mesaChamadaGarcomService.chamarGarcom(request, criarAuthenticationMesa());

        assertSame(notificacao, response);
        verify(mesaClient).validarSessaoMesa(1, 123456);
        verify(notificacaoService).criarNotificacao(new CriarNotificacaoRequest(
                TipoNotificacao.CHAMADA_GARCOM,
                DestinoNotificacao.GARCOM,
                1,
                8,
                7
        ));
    }

    @Test
    void chamarGarcomDeveBloquearMesaSemGarcomResponsavel() {
        ChamarGarcomRequest request = new ChamarGarcomRequest(123456);
        SessaoMesaResponse sessao = new SessaoMesaResponse(1, 8, 123456, null, "OCUPADA");

        when(mesaClient.validarSessaoMesa(1, 123456)).thenReturn(sessao);

        BaseException exception = assertThrows(
                BaseException.class,
                () -> mesaChamadaGarcomService.chamarGarcom(request, criarAuthenticationMesa())
        );

        assertEquals(ErrorEnum.MESA_SEM_GARCOM, exception.getErrorEnum());
        verify(mesaClient).validarSessaoMesa(1, 123456);
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void chamarGarcomDeveMapearSessaoMesaInvalida() {
        ChamarGarcomRequest request = new ChamarGarcomRequest(123456);

        when(mesaClient.validarSessaoMesa(1, 123456)).thenThrow(feignException(400));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> mesaChamadaGarcomService.chamarGarcom(request, criarAuthenticationMesa())
        );

        assertEquals(ErrorEnum.SESSAO_MESA_INVALIDA, exception.getErrorEnum());
        verify(mesaClient).validarSessaoMesa(1, 123456);
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void chamarGarcomDeveBloquearUsuarioMesaSemVinculo() {
        ChamarGarcomRequest request = new ChamarGarcomRequest(123456);
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
                () -> mesaChamadaGarcomService.chamarGarcom(request, authentication)
        );

        assertEquals(ErrorEnum.ACESSO_NEGADO, exception.getErrorEnum());
        verifyNoInteractions(mesaClient, notificacaoService);
    }

    @Test
    void chamarGarcomDeveBloquearPerfilDiferenteDeMesa() {
        ChamarGarcomRequest request = new ChamarGarcomRequest(123456);
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
                () -> mesaChamadaGarcomService.chamarGarcom(request, authentication)
        );

        assertEquals(ErrorEnum.ACESSO_NEGADO, exception.getErrorEnum());
        verifyNoInteractions(mesaClient, notificacaoService);
    }

    private NotificacaoResponse criarNotificacaoResponse() {
        return new NotificacaoResponse(
                3,
                "CHAMADA_GARCOM",
                "Cliente solicitou atendimento",
                DestinoNotificacao.GARCOM,
                false,
                LocalDateTime.of(2026, 7, 2, 10, 15, 30),
                1,
                8,
                7
        );
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
}
