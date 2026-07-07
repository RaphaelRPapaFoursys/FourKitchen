package br.com.fourkitchen.bff_restaurante.security;

import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, new ObjectMapper());
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterDeveContinuarSemAutenticarQuandoHeaderAuthorizationNaoExistir() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterDeveContinuarSemAutenticarQuandoHeaderAuthorizationNaoForBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterDeveAutenticarUsuarioQuandoTokenForValido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer jwt-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        UsuarioAutenticado usuarioAutenticado = new UsuarioAutenticado(
                1L,
                "Lucas",
                "garcom@fourkitchen.com",
                "GARCOM"
        );

        when(jwtService.extrairUsuario("jwt-token")).thenReturn(usuarioAutenticado);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertInstanceOf(
                UsuarioAutenticado.class,
                SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        assertEquals(
                usuarioAutenticado,
                SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        assertTrue(SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(authority -> "ROLE_GARCOM".equals(authority.getAuthority())));
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extrairUsuario("jwt-token");
    }

    @Test
    void doFilterDeveRetornarUnauthorizedQuandoTokenForInvalido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer jwt-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        when(jwtService.extrairUsuario("jwt-invalido")).thenThrow(new RuntimeException("token invalido"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith(MediaType.APPLICATION_JSON_VALUE));
        assertTrue(response.getContentAsString().contains(ErrorEnum.TOKEN_INVALIDO.getErrorCode()));
        assertTrue(response.getContentAsString().contains(ErrorEnum.TOKEN_INVALIDO.getErrorMessage()));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);
        verify(jwtService).extrairUsuario("jwt-invalido");
    }
}
