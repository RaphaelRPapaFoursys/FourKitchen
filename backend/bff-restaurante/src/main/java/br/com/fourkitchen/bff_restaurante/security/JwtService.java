package br.com.fourkitchen.bff_restaurante.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    public void validarToken(String token) {
        extrairClaims(token);
    }

    public UsuarioAutenticado extrairUsuario(String token) {
        Claims claims = extrairClaims(token);

        return new UsuarioAutenticado(
                extrairId(claims),
                claims.get("nome", String.class),
                claims.getSubject(),
                claims.get("perfil", String.class)
        );
    }

    private Long extrairId(Claims claims) {
        Object id = claims.get("id");

        if (id instanceof Number number) {
            return number.longValue();
        }

        return Long.valueOf(id.toString());
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
