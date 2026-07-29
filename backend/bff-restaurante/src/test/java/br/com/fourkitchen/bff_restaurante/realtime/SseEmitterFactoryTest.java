package br.com.fourkitchen.bff_restaurante.realtime;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SseEmitterFactoryTest {

    @Test
    void deveCriarEmitterComTimeoutDeTrintaMinutos() {
        SseEmitter emitter = new SseEmitterFactory().criar();

        assertEquals(30 * 60 * 1000L, emitter.getTimeout());
    }
}
