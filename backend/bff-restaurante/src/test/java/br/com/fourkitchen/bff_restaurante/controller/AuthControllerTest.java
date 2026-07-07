package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.LoginRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.LoginResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.UsuarioAutenticadoResponse;
import br.com.fourkitchen.bff_restaurante.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginDeveRetornarTokenGeradoPeloUserService() {
        LoginRequest request = new LoginRequest("garcom@fourkitchen.com", "123456");
        LoginResponse loginResponse = new LoginResponse(
                "jwt-token",
                "Bearer",
                new UsuarioAutenticadoResponse(1L, "Lucas", "garcom@fourkitchen.com", "GARCOM")
        );

        when(userService.login(request)).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertSame(loginResponse, response.getBody());
        verify(userService).login(request);
    }

    @Test
    void meDeveRetornarUsuarioAutenticado() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "principal",
                null,
                List.of()
        );
        UsuarioAutenticadoResponse usuarioResponse = new UsuarioAutenticadoResponse(
                1L,
                "Lucas",
                "garcom@fourkitchen.com",
                "GARCOM"
        );

        when(userService.me(authentication)).thenReturn(usuarioResponse);

        ResponseEntity<UsuarioAutenticadoResponse> response = authController.me(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertSame(usuarioResponse, response.getBody());
        verify(userService).me(authentication);
    }
}
