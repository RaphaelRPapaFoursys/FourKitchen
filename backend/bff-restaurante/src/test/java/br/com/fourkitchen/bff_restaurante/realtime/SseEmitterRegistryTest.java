package br.com.fourkitchen.bff_restaurante.realtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SseEmitterRegistryTest {

    private SseEmitterFactory emitterFactory;
    private SseEmitter emitter;
    private SseEmitterRegistry registry;

    @BeforeEach
    void setUp() {
        emitterFactory = mock(SseEmitterFactory.class);
        emitter = mock(SseEmitter.class);
        registry = new SseEmitterRegistry(emitterFactory);
        when(emitterFactory.criar()).thenReturn(emitter);
    }

    @Test
    void deveRegistrarEPublicarEvento() throws IOException {
        SseEmitter resultado = registry.registrar("COZINHA");
        EventoRealtime evento = EventoRealtime.criar(
                TipoEventoRealtime.PEDIDO_CRIADO, 1, null, null, null, "pedido"
        );

        registry.publicar("COZINHA", evento);

        assertSame(emitter, resultado);
        assertEquals(1, registry.totalConexoes("COZINHA"));
        verify(emitter, org.mockito.Mockito.times(2)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void deveRemoverEmitterAoCompletar() {
        ArgumentCaptor<Runnable> callback = ArgumentCaptor.forClass(Runnable.class);
        registry.registrar("COZINHA");
        verify(emitter).onCompletion(callback.capture());

        callback.getValue().run();
        callback.getValue().run();

        assertEquals(0, registry.totalConexoes("COZINHA"));
    }

    @Test
    void deveRemoverEmitterAoExpirar() {
        ArgumentCaptor<Runnable> callback = ArgumentCaptor.forClass(Runnable.class);
        registry.registrar("COZINHA");
        verify(emitter).onTimeout(callback.capture());

        callback.getValue().run();

        assertEquals(0, registry.totalConexoes("COZINHA"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void deveRemoverEmitterAoReceberErro() {
        ArgumentCaptor<Consumer<Throwable>> callback = ArgumentCaptor.forClass(Consumer.class);
        registry.registrar("COZINHA");
        verify(emitter).onError(callback.capture());

        callback.getValue().accept(new IOException("conexao encerrada"));

        assertEquals(0, registry.totalConexoes("COZINHA"));
    }

    @Test
    void deveRemoverEmitterQuandoFalharAoEnviarEvento() throws IOException {
        registry.registrar("COZINHA");
        doThrow(new IOException("conexao encerrada"))
                .when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        registry.publicar("COZINHA", EventoRealtime.criar(
                TipoEventoRealtime.PEDIDO_ATUALIZADO, 1, null, null, null, null
        ));

        assertEquals(0, registry.totalConexoes("COZINHA"));
        verify(emitter).complete();
    }

    @Test
    void deveRemoverEmitterQuandoFalharNaConexaoInicial() throws IOException {
        doThrow(new IllegalStateException("emitter encerrado"))
                .when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        registry.registrar("COZINHA");

        assertEquals(0, registry.totalConexoes("COZINHA"));
        verify(emitter).complete();
    }

    @Test
    void publicarSemAssinantesNaoDeveEnviar() throws IOException {
        registry.publicar("COZINHA", EventoRealtime.criar(
                TipoEventoRealtime.PEDIDO_PRONTO, 1, null, null, null, null
        ));

        verify(emitter, never()).send(any(SseEmitter.SseEventBuilder.class));
    }
}
