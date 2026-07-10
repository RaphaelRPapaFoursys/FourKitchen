package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.usuarios.UsuarioClient;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.AtualizarUsuarioClientRequest;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.CriarUsuarioClientRequest;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarUsuarioRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarUsuarioRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.UsuarioGestorResponse;
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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class GestorUsuarioServiceTest {

    private static final String AUTHORIZATION = "Bearer token";

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private GestorUsuarioService gestorUsuarioService;

    @Test
    void listarUsuariosDeveRetornarUsuariosOrdenadosPorNome() {
        UsuarioClientResponse bruno = criarUsuario(2, "Bruno Silva", "bruno@fourkitchen.com", "GARCOM", true);
        UsuarioClientResponse amanda = criarUsuario(1, "Amanda Souza", "amanda@fourkitchen.com", "GESTOR", true);

        when(usuarioClient.listarUsuariosAtivos(AUTHORIZATION)).thenReturn(List.of(bruno, amanda));

        List<UsuarioGestorResponse> resultado = gestorUsuarioService.listarUsuarios(AUTHORIZATION);

        assertEquals(2, resultado.size());
        assertEquals("Amanda Souza", resultado.get(0).nome());
        assertEquals("Bruno Silva", resultado.get(1).nome());
        verify(usuarioClient).listarUsuariosAtivos(AUTHORIZATION);
    }

    @Test
    void criarUsuarioDeveDelegarParaMsUsuarios() {
        CriarUsuarioRequest request = new CriarUsuarioRequest(
                "Amanda Souza",
                "amanda@fourkitchen.com",
                "Senha123",
                "GESTOR",
                null
        );
        UsuarioClientResponse usuarioCriado = criarUsuario(
                1,
                "Amanda Souza",
                "amanda@fourkitchen.com",
                "GESTOR",
                true
        );

        when(usuarioClient.criarUsuario(
                org.mockito.ArgumentMatchers.any(CriarUsuarioClientRequest.class),
                org.mockito.ArgumentMatchers.eq(AUTHORIZATION)
        )).thenReturn(usuarioCriado);

        UsuarioGestorResponse resultado = gestorUsuarioService.criarUsuario(request, AUTHORIZATION);

        assertEquals(1, resultado.id());
        assertEquals("Amanda Souza", resultado.nome());

        ArgumentCaptor<CriarUsuarioClientRequest> requestCaptor =
                ArgumentCaptor.forClass(CriarUsuarioClientRequest.class);
        verify(usuarioClient).criarUsuario(
                requestCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(AUTHORIZATION)
        );
        assertEquals("Senha123", requestCaptor.getValue().senha());
        assertEquals("GESTOR", requestCaptor.getValue().perfilUsuario());
        assertEquals(null, requestCaptor.getValue().idMesa());
    }

    @Test
    void criarUsuarioDeveMapearEmailDuplicado() {
        CriarUsuarioRequest request = new CriarUsuarioRequest(
                "Amanda Souza",
                "amanda@fourkitchen.com",
                "Senha123",
                "GESTOR",
                null
        );

        when(usuarioClient.criarUsuario(
                org.mockito.ArgumentMatchers.any(CriarUsuarioClientRequest.class),
                org.mockito.ArgumentMatchers.eq(AUTHORIZATION)
        )).thenThrow(feignException(409));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> gestorUsuarioService.criarUsuario(request, AUTHORIZATION)
        );

        assertEquals(ErrorEnum.EMAIL_JA_CADASTRADO, exception.getErrorEnum());
    }

    @Test
    void atualizarUsuarioDeveDelegarParaMsUsuarios() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
                "Amanda Souza",
                "amanda@fourkitchen.com",
                "NovaSenha123",
                "GESTOR",
                null
        );
        UsuarioClientResponse usuarioAtualizado = criarUsuario(
                1,
                "Amanda Souza",
                "amanda@fourkitchen.com",
                "GESTOR",
                true
        );

        when(usuarioClient.atualizarUsuario(
                org.mockito.ArgumentMatchers.eq(1),
                org.mockito.ArgumentMatchers.any(AtualizarUsuarioClientRequest.class),
                org.mockito.ArgumentMatchers.eq(AUTHORIZATION)
        )).thenReturn(usuarioAtualizado);

        UsuarioGestorResponse resultado = gestorUsuarioService.atualizarUsuario(1, request, AUTHORIZATION);

        assertEquals(1, resultado.id());
        assertEquals("Amanda Souza", resultado.nome());

        ArgumentCaptor<AtualizarUsuarioClientRequest> requestCaptor =
                ArgumentCaptor.forClass(AtualizarUsuarioClientRequest.class);
        verify(usuarioClient).atualizarUsuario(
                org.mockito.ArgumentMatchers.eq(1),
                requestCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(AUTHORIZATION)
        );
        assertEquals("NovaSenha123", requestCaptor.getValue().senha());
        assertEquals("GESTOR", requestCaptor.getValue().perfilUsuario());
    }

    @Test
    void atualizarUsuarioDeveMapearEmailDuplicado() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
                "Amanda Souza",
                "amanda@fourkitchen.com",
                null,
                "GESTOR",
                null
        );

        when(usuarioClient.atualizarUsuario(
                org.mockito.ArgumentMatchers.eq(1),
                org.mockito.ArgumentMatchers.any(AtualizarUsuarioClientRequest.class),
                org.mockito.ArgumentMatchers.eq(AUTHORIZATION)
        )).thenThrow(feignException(409));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> gestorUsuarioService.atualizarUsuario(1, request, AUTHORIZATION)
        );

        assertEquals(ErrorEnum.EMAIL_JA_CADASTRADO, exception.getErrorEnum());
    }

    @Test
    void inativarUsuarioDeveDelegarParaMsUsuarios() {
        Authentication authentication = authentication(99L);

        gestorUsuarioService.inativarUsuario(1, AUTHORIZATION, authentication);

        verify(usuarioClient).inativarUsuario(1, AUTHORIZATION);
    }

    @Test
    void inativarUsuarioDeveBloquearProprioUsuario() {
        Authentication authentication = authentication(1L);

        BaseException exception = assertThrows(
                BaseException.class,
                () -> gestorUsuarioService.inativarUsuario(1, AUTHORIZATION, authentication)
        );

        assertEquals(ErrorEnum.NAO_PODE_EXCLUIR_PROPRIO_USUARIO, exception.getErrorEnum());
        verifyNoInteractions(usuarioClient);
    }

    @Test
    void inativarUsuarioDeveMapearUsuarioNaoEncontrado() {
        Authentication authentication = authentication(99L);

        doThrow(feignException(404)).when(usuarioClient).inativarUsuario(1, AUTHORIZATION);

        BaseException exception = assertThrows(
                BaseException.class,
                () -> gestorUsuarioService.inativarUsuario(1, AUTHORIZATION, authentication)
        );

        assertEquals(ErrorEnum.USUARIO_NAO_ENCONTRADO, exception.getErrorEnum());
    }

    @Test
    void listarUsuariosDeveLancarTokenInvalidoQuandoAuthorizationNaoForBearer() {
        BaseException exception = assertThrows(
                BaseException.class,
                () -> gestorUsuarioService.listarUsuarios("token")
        );

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
        verifyNoInteractions(usuarioClient);
    }

    private Authentication authentication(Long idUsuario) {
        return new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(idUsuario, "Gestor", "gestor@fourkitchen.com", "GESTOR", null),
                null
        );
    }

    private UsuarioClientResponse criarUsuario(
            Integer id,
            String nome,
            String email,
            String perfilUsuario,
            Boolean ativo
    ) {
        return new UsuarioClientResponse(
                id,
                nome,
                email,
                perfilUsuario,
                null,
                ativo
        );
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/usuarios",
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

        return FeignException.errorStatus("usuarios", response);
    }
}
