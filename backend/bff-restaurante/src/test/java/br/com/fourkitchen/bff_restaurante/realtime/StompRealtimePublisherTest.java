package br.com.fourkitchen.bff_restaurante.realtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class StompRealtimePublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private StompRealtimePublisher publisher;

    @Test
    void devePublicarMesmoEventoSemDuplicarDestinos() {
        publisher.publish(
                RealtimeEventType.PEDIDO_CRIADO,
                42,
                Map.of("idMesa", 7),
                "/topic/cozinha/pedidos",
                "/topic/cozinha/pedidos",
                "/topic/mesas/7"
        );

        ArgumentCaptor<RealtimeEvent> eventCaptor =
                ArgumentCaptor.forClass(RealtimeEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        RealtimeEvent event = eventCaptor.getValue();

        assertEquals(RealtimeEventType.PEDIDO_CRIADO, event.type());
        assertEquals("42", event.aggregateId());
        assertEquals(7, event.data().get("idMesa"));
        verify(messagingTemplate).convertAndSend("/topic/cozinha/pedidos", event);
        verify(messagingTemplate).convertAndSend("/topic/mesas/7", event);
        verifyNoMoreInteractions(messagingTemplate);
    }
}
