package br.com.fourkitchen.ms_usuarios.security;

import br.com.fourkitchen.ms_usuarios.enums.PerfilUsuario;
import br.com.fourkitchen.ms_usuarios.model.Usuario;
import io.jsonwebtoken.JwtException;
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
                .senha("senha-criptografada")
                .ativo(true)
                .build();
    }
}
