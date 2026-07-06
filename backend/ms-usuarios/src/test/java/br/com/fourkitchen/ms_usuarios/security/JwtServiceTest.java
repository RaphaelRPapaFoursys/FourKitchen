package br.com.fourkitchen.ms_usuarios.security;

import br.com.fourkitchen.ms_usuarios.enums.PerfilUsuario;
import br.com.fourkitchen.ms_usuarios.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "chave-super-secreta-para-jwt-testes-1234567890";
    private static final long EXPIRATION = 43_200_000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
    }

    @Test
    void gerarTokenDeveRetornarTokenValidoComEmailDoUsuario() {
        Usuario usuario = criarUsuario();

        String token = jwtService.gerarToken(usuario);

        assertNotNull(token);
        assertEquals(usuario.getEmail(), jwtService.extrairEmail(token));
        assertTrue(jwtService.validarToken(token, usuario.getEmail()));
    }

    @Test
    void gerarTokenDeveIncluirIdMesaQuandoUsuarioForDispositivoMesa() {
        Usuario usuario = criarUsuarioMesa();

        String token = jwtService.gerarToken(usuario);

        assertEquals(1, extrairClaims(token).get("idMesa", Integer.class));
    }

    @Test
    void validarTokenDeveRetornarFalseQuandoEmailForDiferente() {
        String token = jwtService.gerarToken(criarUsuario());

        boolean tokenValido = jwtService.validarToken(token, "outro@email.com");

        assertFalse(tokenValido);
    }

    @Test
    void validarTokenDeveLancarExcecaoQuandoAssinaturaForInvalida() {
        String token = jwtService.gerarToken(criarUsuario());
        JwtService outroJwtService = new JwtService();
        ReflectionTestUtils.setField(outroJwtService, "secret", "outra-chave-super-secreta-para-jwt-testes-123456");
        ReflectionTestUtils.setField(outroJwtService, "expiration", EXPIRATION);

        assertThrows(JwtException.class, () -> outroJwtService.validarToken(token, "ana@email.com"));
    }

    private Usuario criarUsuario() {
        return Usuario.builder()
                .id(1)
                .nome("Ana")
                .email("ana@email.com")
                .perfilUsuario(PerfilUsuario.ADMIN)
                .idMesa(null)
                .senha("senha-criptografada")
                .ativo(true)
                .build();
    }

    private Usuario criarUsuarioMesa() {
        return Usuario.builder()
                .id(10)
                .nome("Mesa 1")
                .email("mesa01@fourkitchen.com")
                .perfilUsuario(PerfilUsuario.MESA)
                .idMesa(1)
                .senha("senha-criptografada")
                .ativo(true)
                .build();
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
