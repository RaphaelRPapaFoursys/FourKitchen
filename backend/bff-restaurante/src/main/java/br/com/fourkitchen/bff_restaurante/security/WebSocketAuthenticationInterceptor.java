package br.com.fourkitchen.bff_restaurante.security;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketAuthenticationInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );
        if (accessor == null) {
            return message;
        }
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            authenticate(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            authorizeSubscription(accessor);
        } else if (StompCommand.SEND.equals(command)) {
            throw new AccessDeniedException("WebSocket aceita apenas notificacoes do servidor.");
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new AccessDeniedException("Token WebSocket ausente.");
        }

        try {
            UsuarioAutenticado usuario = jwtService.extrairUsuario(
                    authorization.substring(BEARER_PREFIX.length())
            );
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    usuario,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + usuario.perfil()))
            );
            accessor.setUser(authentication);
        } catch (Exception exception) {
            throw new AccessDeniedException("Token WebSocket invalido.", exception);
        }
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof Authentication authentication)
                || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new AccessDeniedException("Conexao WebSocket nao autenticada.");
        }

        String destination = accessor.getDestination();
        if (destination == null || !canSubscribe(usuario, destination)) {
            throw new AccessDeniedException("Topico WebSocket nao autorizado.");
        }
    }

    private boolean canSubscribe(UsuarioAutenticado usuario, String destination) {
        if ("ADMIN".equals(usuario.perfil())) {
            return destination.startsWith("/topic/");
        }

        return switch (usuario.perfil()) {
            case "COZINHA" -> destination.equals("/topic/cozinha/pedidos");
            case "GARCOM" -> destination.equals("/topic/garcom/operacao")
                    || destination.equals("/topic/cardapio");
            case "GESTOR" -> destination.startsWith("/topic/gestor/")
                    || destination.equals("/topic/cardapio");
            case "TOTEM" -> destination.equals("/topic/cardapio");
            case "MESA" -> destination.equals("/topic/cardapio")
                    || usuario.idMesa() != null
                    && destination.equals("/topic/mesas/" + usuario.idMesa());
            default -> false;
        };
    }
}
