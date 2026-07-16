package br.com.fourkitchen.bff_restaurante.realtime;

import java.util.Map;

public interface RealtimePublisher {

    void publish(
            RealtimeEventType type,
            Object aggregateId,
            Map<String, Object> data,
            String... destinations
    );
}
