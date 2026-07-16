package br.com.fourkitchen.bff_restaurante.realtime;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RealtimeEvent(
        UUID eventId,
        RealtimeEventType type,
        Instant occurredAt,
        String aggregateId,
        Map<String, Object> data
) {
    public static RealtimeEvent create(
            RealtimeEventType type,
            Object aggregateId,
            Map<String, Object> data
    ) {
        return new RealtimeEvent(
                UUID.randomUUID(),
                type,
                Instant.now(),
                aggregateId == null ? null : aggregateId.toString(),
                data == null ? Map.of() : Map.copyOf(data)
        );
    }
}
