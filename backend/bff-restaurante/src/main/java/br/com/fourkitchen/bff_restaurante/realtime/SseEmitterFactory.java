package br.com.fourkitchen.bff_restaurante.realtime;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterFactory {

    private static final long TIMEOUT_MILLIS = 30 * 60 * 1000L;

    public SseEmitter criar() {
        return new SseEmitter(TIMEOUT_MILLIS);
    }
}
