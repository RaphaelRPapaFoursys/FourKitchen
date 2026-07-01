package br.com.fourkitchen.bff_restaurante.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "chave-super-secreta-para-testes-fourkitchen-123456789";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    }

    @Test
    void validarTokenDeveAceitarJwtValido() {
        String token = gerarToken(1L, new Date(System.currentTimeMillis() + 60000L), SECRET);

        assertDoesNotThrow(() -> jwtService.validarToken(token));
    }

    @Test
    void extrairUsuarioDeveRetornarClaimsDoJwt() {
        String token = gerarToken(1L, new Date(System.currentTimeMillis() + 60000L), SECRET);

        UsuarioAutenticado usuarioAutenticado = jwtService.extrairUsuario(token);

        assertEquals(1L, usuarioAutenticado.id());
        assertEquals("Lucas", usuarioAutenticado.nome());
        assertEquals("garcom@fourkitchen.com", usuarioAutenticado.email());
        assertEquals("GARCOM", usuarioAutenticado.perfil());
    }

    @Test
    void extrairUsuarioDeveConverterIdTextoParaLong() {
        String token = gerarToken("2", new Date(System.currentTimeMillis() + 60000L), SECRET);

        UsuarioAutenticado usuarioAutenticado = jwtService.extrairUsuario(token);

        assertEquals(2L, usuarioAutenticado.id());
    }

    @Test
    void validarTokenDeveRejeitarJwtExpirado() {
        String token = gerarToken(1L, new Date(System.currentTimeMillis() - 60000L), SECRET);

        assertThrows(Exception.class, () -> jwtService.validarToken(token));
    }

    @Test
    void validarTokenDeveRejeitarJwtComAssinaturaInvalida() {
        String token = gerarToken(1L, new Date(System.currentTimeMillis() + 60000L), SECRET + "assinatura-diferente");

        assertThrows(Exception.class, () -> jwtService.validarToken(token));
    }

    private String gerarToken(Object id, Date expiration, String secret) {
        return Jwts.builder()
                .subject("garcom@fourkitchen.com")
                .claim("id", id)
                .claim("nome", "Lucas")
                .claim("perfil", "GARCOM")
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }
}
