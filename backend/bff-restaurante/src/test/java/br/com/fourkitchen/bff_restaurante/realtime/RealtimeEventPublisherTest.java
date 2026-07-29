package br.com.fourkitchen.bff_restaurante.realtime;

import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.SinalizarProblemaResponse;
import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.enums.StatusProdutoPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RealtimeEventPublisherTest {

    private SseEmitterRegistry registry;
    private RealtimeEventPublisher publisher;

    @BeforeEach
    void setUp() {
        registry = mock(SseEmitterRegistry.class);
        publisher = new RealtimeEventPublisher(registry);
    }

    @Test
    void pedidoCriadoPorGarcomDeveIrAosCanaisRelevantes() {
        PedidoResponse pedido = pedido("GARCOM", 1, 7);

        publisher.pedidoCriado(pedido);

        EventoRealtime evento = capturarEvento("GARCOM:7");
        verify(registry).publicar("MESA:1", evento);
        verificarCanaisPedido(evento);
        assertEquals(TipoEventoRealtime.PEDIDO_CRIADO, evento.tipo());
        assertEquals(25, evento.idPedido());
        assertEquals(7, evento.idGarcom());
        assertNotNull(evento.data());
        assertSame(pedido, evento.payload());
    }

    @Test
    void pedidoAtualizadoDaMesaNaoDeveSerEnviadoComoEventoDoGarcom() {
        PedidoResponse pedido = pedido("MESA", 1, 101);

        publisher.pedidoAtualizado(pedido);

        EventoRealtime evento = capturarEvento("MESA:1");
        verificarCanaisPedido(evento);
        verify(registry, times(4)).publicar(any(), any());
        verify(registry, never()).publicar("GARCOM:101", evento);
        assertEquals(TipoEventoRealtime.PEDIDO_ATUALIZADO, evento.tipo());
        assertNull(evento.idGarcom());
    }

    @Test
    void pedidoProntoDoTotemDeveIrAoTotemEBalcao() {
        PedidoResponse pedido = pedido("TOTEM", null, 201);

        publisher.pedidoPronto(pedido);

        EventoRealtime evento = capturarEvento("TOTEM:201");
        verify(registry).publicar(CanalRealtime.BALCAO, evento);
        verificarCanaisPedido(evento);
        assertEquals(TipoEventoRealtime.PEDIDO_PRONTO, evento.tipo());
    }

    @Test
    void problemaCozinhaDeveUsarPedidoParaRotearEPayloadDoProblema() {
        PedidoResponse pedido = pedido("GARCOM", 1, 7);
        SinalizarProblemaResponse problema = new SinalizarProblemaResponse(
                25, 10, "AGUARDANDO_DECISAO", StatusProdutoPedido.ERRO
        );

        publisher.problemaCozinha(pedido, problema);

        EventoRealtime evento = capturarEvento("GARCOM:7");
        verify(registry).publicar("MESA:1", evento);
        assertEquals(TipoEventoRealtime.PROBLEMA_COZINHA, evento.tipo());
        assertSame(problema, evento.payload());
    }

    @Test
    void chamadaGarcomDeveIrSomenteAosDestinatariosIdentificadosEGlobais() {
        NotificacaoResponse notificacao = new NotificacaoResponse(
                3, "CHAMADA_GARCOM", "Atendimento solicitado", DestinoNotificacao.GARCOM,
                false, LocalDateTime.now(), 1, 8, 7
        );

        publisher.chamadaGarcom(notificacao);

        EventoRealtime evento = capturarEvento("GARCOM:7");
        verify(registry).publicar("MESA:1", evento);
        verificarCanaisGestao(evento);
        verify(registry, never()).publicar(CanalRealtime.COZINHA, evento);
        assertEquals(TipoEventoRealtime.CHAMADA_GARCOM, evento.tipo());
        assertNull(evento.idPedido());
        assertSame(notificacao, evento.payload());
    }

    @Test
    void chamadaSemIdentificadoresDeveIrSomenteAosCanaisGlobais() {
        NotificacaoResponse notificacao = new NotificacaoResponse(
                3, "CHAMADA_GARCOM", "Atendimento solicitado", DestinoNotificacao.GARCOM,
                false, LocalDateTime.now(), null, null, null
        );

        publisher.chamadaGarcom(notificacao);

        verify(registry, times(2)).publicar(any(), any());
        verify(registry, never()).publicar(org.mockito.ArgumentMatchers.startsWith("MESA:"), any());
        verify(registry, never()).publicar(org.mockito.ArgumentMatchers.startsWith("GARCOM:"), any());
    }

    private PedidoResponse pedido(String canal, Integer idMesa, Integer idUsuario) {
        return new PedidoResponse(25, 100025, canal, "ENVIADO_COZINHA", idMesa, idUsuario, 8);
    }

    private EventoRealtime capturarEvento(String canal) {
        ArgumentCaptor<EventoRealtime> captor = ArgumentCaptor.forClass(EventoRealtime.class);
        verify(registry).publicar(org.mockito.ArgumentMatchers.eq(canal), captor.capture());
        return captor.getValue();
    }

    private void verificarCanaisPedido(EventoRealtime evento) {
        verify(registry).publicar(CanalRealtime.COZINHA, evento);
        verificarCanaisGestao(evento);
    }

    private void verificarCanaisGestao(EventoRealtime evento) {
        verify(registry).publicar(CanalRealtime.GESTOR, evento);
        verify(registry).publicar(CanalRealtime.ADMIN, evento);
    }
}
