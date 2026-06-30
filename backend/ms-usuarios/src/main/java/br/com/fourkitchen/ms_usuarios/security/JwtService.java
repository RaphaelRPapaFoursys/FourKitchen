package br.com.fourkitchen.ms_usuarios.security;

import br.com.fourkitchen.ms_usuarios.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Service responsible for generating and validating JWT access tokens for users.
 *
 * <p>The signing secret and expiration time are provided by application
 * configuration through the {@code jwt.secret} and {@code jwt.expiration-ms}
 * properties.</p>
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expiration;

    /**
     * Generates a signed JWT containing the user's e-mail as subject and basic
     * identity claims used by the application.
     *
     * @param usuario authenticated user used as token source
     * @return signed JWT string
     */
    public String gerarToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("perfil", usuario.getPerfilUsuario().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    /**
     * Extracts the user e-mail stored as the JWT subject.
     *
     * @param token JWT string
     * @return e-mail stored in the token subject
     */
    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    /**
     * Validates whether the token belongs to the expected e-mail and is not expired.
     *
     * @param token JWT string
     * @param email expected user e-mail
     * @return {@code true} when the token is valid for the e-mail
     */
    public boolean validarToken(String token, String email) {
        return extrairEmail(token).equals(email) && !isTokenExpirado(token);
    }

    private boolean isTokenExpirado(String token) {
        return extrairClaims(token).getExpiration().before(new Date());
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
