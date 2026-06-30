package br.com.fourkitchen.ms_usuarios.security;

import br.com.fourkitchen.ms_usuarios.exception.ErrorEnum;
import br.com.fourkitchen.ms_usuarios.exception.ErrorObject;
import br.com.fourkitchen.ms_usuarios.model.Usuario;
import br.com.fourkitchen.ms_usuarios.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Servlet filter that authenticates requests containing a bearer JWT.
 *
 * <p>When the token is valid, the filter loads the active user and stores an
 * authenticated {@link UsernamePasswordAuthenticationToken} in the Spring
 * Security context. Invalid tokens are answered with the application's standard
 * JSON error format.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    /**
     * Reads the {@code Authorization} header and authenticates the request when a
     * valid bearer token is present.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param filterChain remaining servlet filter chain
     * @throws ServletException when the downstream filter chain fails
     * @throws IOException when writing the response fails
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String email = jwtService.extrairEmail(token);

            if (email == null) {
                writeErrorResponse(response, ErrorEnum.TOKEN_INVALIDO);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                        .orElse(null);

                if (usuario == null || !jwtService.validarToken(token, email)) {
                    writeErrorResponse(response, ErrorEnum.TOKEN_INVALIDO);
                    return;
                }

                if (!Boolean.TRUE.equals(usuario.getAtivo())) {
                    writeErrorResponse(response, ErrorEnum.USUARIO_INATIVO);
                    return;
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                usuario,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfilUsuario().name()))
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            writeErrorResponse(response, ErrorEnum.TOKEN_INVALIDO);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorEnum errorEnum) throws IOException {
        ErrorObject errorObject = ErrorObject.builder()
                .codError(errorEnum.getErrorCode())
                .msgError(errorEnum.getErrorMessage())
                .build();

        response.setStatus(errorEnum.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), errorObject);
    }
}
