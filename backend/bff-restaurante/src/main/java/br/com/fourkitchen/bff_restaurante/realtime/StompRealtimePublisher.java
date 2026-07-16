package br.com.fourkitchen.bff_restaurante.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class StompRealtimePublisher implements RealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(
            RealtimeEventType type,
            Object aggregateId,
            Map<String, Object> data,
            String... destinations
    ) {
        RealtimeEvent event = RealtimeEvent.create(type, aggregateId, data);
        applicationEventPublisher.publishEvent(event);

        Arrays.stream(destinations)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(destination -> messagingTemplate.convertAndSend(destination, event));
    }
}
