package br.com.fourkitchen.bff_restaurante.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SseEmitterRegistry {

    private final SseEmitterFactory emitterFactory;

    private final Map<String, Set<SseEmitter>> emittersPorCanal = new ConcurrentHashMap<>();

    public SseEmitter registrar(String canal) {
        SseEmitter emitter = emitterFactory.criar();
        emittersPorCanal.computeIfAbsent(canal, ignored -> ConcurrentHashMap.newKeySet()).add(emitter);

        emitter.onCompletion(() -> remover(canal, emitter));
        emitter.onTimeout(() -> remover(canal, emitter));
        emitter.onError(erro -> remover(canal, emitter));

        try {
            emitter.send(SseEmitter.event().comment("conectado"));
        } catch (IOException | IllegalStateException exception) {
            remover(canal, emitter);
            emitter.complete();
        }

        return emitter;
    }

    public void publicar(String canal, EventoRealtime evento) {
        for (SseEmitter emitter : emittersPorCanal.getOrDefault(canal, Set.of())) {
            try {
                emitter.send(SseEmitter.event()
                        .name(evento.tipo().name())
                        .data(evento));
            } catch (IOException | IllegalStateException exception) {
                remover(canal, emitter);
                emitter.complete();
            }
        }
    }

    int totalConexoes(String canal) {
        return emittersPorCanal.getOrDefault(canal, Set.of()).size();
    }

    private void remover(String canal, SseEmitter emitter) {
        Set<SseEmitter> emitters = emittersPorCanal.get(canal);
        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersPorCanal.remove(canal, emitters);
        }
    }
}
