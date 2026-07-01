package br.com.fourkitchen.bff_restaurante.config;

import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig(
            mock(JwtAuthenticationFilter.class),
            new ObjectMapper()
    );

    @Test
    void handleAuthenticationErrorDeveRetornarTokenInvalido() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        securityConfig.handleAuthenticationError(
                new MockHttpServletRequest(),
                response,
                new BadCredentialsException("token invalido")
        );

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains(ErrorEnum.TOKEN_INVALIDO.getErrorCode()));
        assertTrue(response.getContentAsString().contains(ErrorEnum.TOKEN_INVALIDO.getErrorMessage()));
    }

    @Test
    void handleAccessDeniedDeveRetornarAcessoNegado() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        securityConfig.handleAccessDenied(
                new MockHttpServletRequest(),
                response,
                new AccessDeniedException("acesso negado")
        );

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains(ErrorEnum.ACESSO_NEGADO.getErrorCode()));
        assertTrue(response.getContentAsString().contains(ErrorEnum.ACESSO_NEGADO.getErrorMessage()));
    }

    @Test
    void userDetailsServiceDeveRejeitarAutenticacaoForaDoJwt() {
        assertThrows(
                UsernameNotFoundException.class,
                () -> securityConfig.userDetailsService().loadUserByUsername("usuario")
        );
    }
}
