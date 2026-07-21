package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarUsuarioRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarUsuarioRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.UsuarioGestorResponse;
import br.com.fourkitchen.bff_restaurante.service.GestorUsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorUsuarioControllerTest {

    private static final String AUTHORIZATION = "Bearer token";

    @Mock
    private GestorUsuarioService gestorUsuarioService;

    @InjectMocks
    private GestorUsuarioController gestorUsuarioController;

    @Test
    void criarUsuarioDeveRetornarCreated() {
        CriarUsuarioRequest request = new CriarUsuarioRequest(
                "Maria Silva",
                "maria@fourkitchen.com",
                "Senha123",
                "GESTOR",
                null
        );
        UsuarioGestorResponse usuario = criarUsuario();

        when(gestorUsuarioService.criarUsuario(request, AUTHORIZATION)).thenReturn(usuario);

        ResponseEntity<UsuarioGestorResponse> response = gestorUsuarioController.criarUsuario(
                request,
                AUTHORIZATION
        );

        assertEquals(201, response.getStatusCode().value());
        assertSame(usuario, response.getBody());
        verify(gestorUsuarioService).criarUsuario(request, AUTHORIZATION);
    }

    @Test
    void listarUsuariosDeveRetornarOk() {
        UsuarioGestorResponse usuario = criarUsuario();

        when(gestorUsuarioService.listarUsuarios(AUTHORIZATION)).thenReturn(List.of(usuario));

        ResponseEntity<List<UsuarioGestorResponse>> response = gestorUsuarioController.listarUsuarios(AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(usuario, response.getBody().getFirst());
        verify(gestorUsuarioService).listarUsuarios(AUTHORIZATION);
    }

    @Test
    void atualizarUsuarioDeveRetornarOk() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
                "Maria Silva",
                "maria@fourkitchen.com",
                null,
                "GESTOR",
                null
        );
        UsuarioGestorResponse usuario = criarUsuario();

        when(gestorUsuarioService.atualizarUsuario(1, request, AUTHORIZATION)).thenReturn(usuario);

        ResponseEntity<UsuarioGestorResponse> response = gestorUsuarioController.atualizarUsuario(
                1,
                request,
                AUTHORIZATION
        );

        assertEquals(200, response.getStatusCode().value());
        assertSame(usuario, response.getBody());
        verify(gestorUsuarioService).atualizarUsuario(1, request, AUTHORIZATION);
    }

    @Test
    void inativarUsuarioDeveRetornarNoContent() {
        Authentication authentication = mock(Authentication.class);

        ResponseEntity<Void> response = gestorUsuarioController.inativarUsuario(1, AUTHORIZATION, authentication);

        assertEquals(204, response.getStatusCode().value());
        verify(gestorUsuarioService).inativarUsuario(1, AUTHORIZATION, authentication);
    }

    @Test
    void ativarUsuarioDeveRetornarOk() {
        UsuarioGestorResponse usuario = criarUsuario();
        when(gestorUsuarioService.ativarUsuario(1, AUTHORIZATION)).thenReturn(usuario);

        ResponseEntity<UsuarioGestorResponse> response = gestorUsuarioController.ativarUsuario(1, AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(usuario, response.getBody());
        verify(gestorUsuarioService).ativarUsuario(1, AUTHORIZATION);
    }

    private UsuarioGestorResponse criarUsuario() {
        return new UsuarioGestorResponse(
                1,
                "Maria Silva",
                "maria@fourkitchen.com",
                "GESTOR",
                null,
                true
        );
    }
}
