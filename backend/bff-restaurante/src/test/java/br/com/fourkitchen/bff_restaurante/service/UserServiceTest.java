package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.usuarios.UsuarioAuthClient;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioLoginRequest;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioLoginResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.LoginRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.LoginResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.UsuarioAutenticadoResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.security.JwtService;
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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UsuarioAuthClient usuarioAuthClient;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void loginDeveDelegarAutenticacaoParaMsUsuariosEValidarToken() {
        LoginRequest request = new LoginRequest("garcom@fourkitchen.com", "123456");
        UsuarioLoginResponse usuarioLoginResponse = new UsuarioLoginResponse(
                "jwt-token",
                1L,
                "Lucas",
                "garcom@fourkitchen.com",
                "GARCOM",
                null
        );

        when(usuarioAuthClient.login(new UsuarioLoginRequest(request.email(), request.senha())))
                .thenReturn(usuarioLoginResponse);

        LoginResponse response = userService.login(request);

        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(new UsuarioAutenticadoResponse(1L, "Lucas", "garcom@fourkitchen.com", "GARCOM", null), response.usuario());
        verify(usuarioAuthClient).login(new UsuarioLoginRequest(request.email(), request.senha()));
        verify(jwtService).validarToken("jwt-token");
    }

    @Test
    void loginDeveLancarCredenciaisInvalidasQuandoMsUsuariosRetornarErroDeAutenticacao() {
        LoginRequest request = new LoginRequest("garcom@fourkitchen.com", "senha-errada");

        when(usuarioAuthClient.login(new UsuarioLoginRequest(request.email(), request.senha())))
                .thenThrow(feignException(401));

        BaseException exception = assertThrows(BaseException.class, () -> userService.login(request));

        assertEquals(ErrorEnum.CREDENCIAIS_INVALIDAS, exception.getErrorEnum());
        verify(usuarioAuthClient).login(new UsuarioLoginRequest(request.email(), request.senha()));
        verifyNoInteractions(jwtService);
    }

    @Test
    void loginDeveLancarServicoIndisponivelQuandoMsUsuariosRetornarErroInesperado() {
        LoginRequest request = new LoginRequest("garcom@fourkitchen.com", "123456");

        when(usuarioAuthClient.login(new UsuarioLoginRequest(request.email(), request.senha())))
                .thenThrow(feignException(500));

        BaseException exception = assertThrows(BaseException.class, () -> userService.login(request));

        assertEquals(ErrorEnum.MS_USUARIOS_INDISPONIVEL, exception.getErrorEnum());
        verify(usuarioAuthClient).login(new UsuarioLoginRequest(request.email(), request.senha()));
        verifyNoInteractions(jwtService);
    }

    @Test
    void loginDeveLancarTokenInvalidoQuandoMsUsuariosRetornarJwtInvalido() {
        LoginRequest request = new LoginRequest("garcom@fourkitchen.com", "123456");
        UsuarioLoginResponse usuarioLoginResponse = new UsuarioLoginResponse(
                "jwt-invalido",
                1L,
                "Lucas",
                "garcom@fourkitchen.com",
                "GARCOM",
                null
        );

        when(usuarioAuthClient.login(new UsuarioLoginRequest(request.email(), request.senha())))
                .thenReturn(usuarioLoginResponse);
        doThrow(new RuntimeException("token invalido")).when(jwtService).validarToken("jwt-invalido");

        BaseException exception = assertThrows(BaseException.class, () -> userService.login(request));

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
        verify(usuarioAuthClient).login(new UsuarioLoginRequest(request.email(), request.senha()));
        verify(jwtService).validarToken("jwt-invalido");
    }

    @Test
    void meDeveRetornarUsuarioAutenticadoDoContexto() {
        UsuarioAutenticado usuarioAutenticado = new UsuarioAutenticado(
                1L,
                "Lucas",
                "garcom@fourkitchen.com",
                "GARCOM",
                null
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                usuarioAutenticado,
                null,
                List.of()
        );

        UsuarioAutenticadoResponse response = userService.me(authentication);

        assertEquals(new UsuarioAutenticadoResponse(1L, "Lucas", "garcom@fourkitchen.com", "GARCOM", null), response);
    }

    @Test
    void meDeveLancarTokenInvalidoQuandoAuthenticationForNulo() {
        BaseException exception = assertThrows(BaseException.class, () -> userService.me(null));

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
    }

    @Test
    void meDeveLancarTokenInvalidoQuandoPrincipalNaoForUsuarioAutenticado() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "principal-invalido",
                null,
                List.of()
        );

        BaseException exception = assertThrows(BaseException.class, () -> userService.me(authentication));

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.POST,
                "/auth/login",
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

        return FeignException.errorStatus("login", response);
    }
}
