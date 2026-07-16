package br.com.fourkitchen.bff_restaurante.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthenticationInterceptorTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private MessageChannel channel;

    @InjectMocks
    private WebSocketAuthenticationInterceptor interceptor;

    @Test
    void deveAutenticarFrameConnectComJwt() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                10L,
                "Mesa 7",
                "mesa7@fourkitchen.com",
                "MESA",
                7
        );
        when(jwtService.extrairUsuario("token-valido")).thenReturn(usuario);
        Message<byte[]> message = connectMessage("Bearer token-valido");

        Message<?> result = interceptor.preSend(message, channel);

        assertSame(message, result);
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
        var authentication = assertInstanceOf(
                UsernamePasswordAuthenticationToken.class,
                accessor.getUser()
        );
        assertSame(usuario, authentication.getPrincipal());
    }

    @Test
    void deveRejeitarConnectSemToken() {
        Message<byte[]> message = connectMessage(null);

        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(message, channel)
        );
    }

    @Test
    void mesaDeveAssinarSomenteSeuProprioTopico() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                10L,
                "Mesa 7",
                "mesa7@fourkitchen.com",
                "MESA",
                7
        );

        assertDoesNotThrow(() -> interceptor.preSend(
                subscribeMessage(usuario, "/topic/mesas/7"),
                channel
        ));
        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(
                        subscribeMessage(usuario, "/topic/mesas/8"),
                        channel
                )
        );
    }

    @Test
    void deveRestringirTopicoPeloPerfil() {
        UsuarioAutenticado cozinha = new UsuarioAutenticado(
                20L,
                "Cozinha",
                "cozinha@fourkitchen.com",
                "COZINHA",
                null
        );

        assertDoesNotThrow(() -> interceptor.preSend(
                subscribeMessage(cozinha, "/topic/cozinha/pedidos"),
                channel
        ));
        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(
                        subscribeMessage(cozinha, "/topic/gestor/operacao"),
                        channel
                )
        );
    }

    @Test
    void deveRejeitarMensagensEnviadasPeloCliente() {
        Message<byte[]> message = stompMessage(StompCommand.SEND, null, null, null);

        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(message, channel)
        );
    }

    private Message<byte[]> connectMessage(String authorization) {
        return stompMessage(StompCommand.CONNECT, authorization, null, null);
    }

    private Message<byte[]> subscribeMessage(
            UsuarioAutenticado usuario,
            String destination
    ) {
        var authentication = new UsernamePasswordAuthenticationToken(usuario, null);
        return stompMessage(StompCommand.SUBSCRIBE, null, destination, authentication);
    }

    private Message<byte[]> stompMessage(
            StompCommand command,
            String authorization,
            String destination,
            UsernamePasswordAuthenticationToken authentication
    ) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        if (authorization != null) {
            accessor.setNativeHeader("Authorization", authorization);
        }
        if (destination != null) {
            accessor.setDestination(destination);
        }
        if (authentication != null) {
            accessor.setUser(authentication);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
