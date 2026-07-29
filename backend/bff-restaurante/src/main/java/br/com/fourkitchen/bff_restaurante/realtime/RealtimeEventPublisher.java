package br.com.fourkitchen.bff_restaurante.realtime;

import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.SinalizarProblemaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RealtimeEventPublisher {

    private static final String CANAL_GARCOM = "GARCOM";
    private static final String CANAL_TOTEM = "TOTEM";

    private final SseEmitterRegistry emitterRegistry;

    public void pedidoCriado(PedidoResponse pedido) {
        publicarPedido(TipoEventoRealtime.PEDIDO_CRIADO, pedido, pedido);
    }

    public void pedidoAtualizado(PedidoResponse pedido) {
        publicarPedido(TipoEventoRealtime.PEDIDO_ATUALIZADO, pedido, pedido);
    }

    public void pedidoPronto(PedidoResponse pedido) {
        publicarPedido(TipoEventoRealtime.PEDIDO_PRONTO, pedido, pedido);
    }

    public void problemaCozinha(PedidoResponse pedido, SinalizarProblemaResponse problema) {
        publicarPedido(TipoEventoRealtime.PROBLEMA_COZINHA, pedido, problema);
    }

    public void chamadaGarcom(NotificacaoResponse notificacao) {
        EventoRealtime evento = EventoRealtime.criar(
                TipoEventoRealtime.CHAMADA_GARCOM,
                null,
                notificacao.idMesa(),
                notificacao.idAtendimento(),
                notificacao.idGarcom(),
                notificacao
        );

        Set<String> canais = canaisGestao();
        adicionarSePresente(canais, notificacao.idMesa(), CanalRealtime::mesa);
        adicionarSePresente(canais, notificacao.idGarcom(), CanalRealtime::garcom);
        publicar(canais, evento);
    }

    private void publicarPedido(TipoEventoRealtime tipo, PedidoResponse pedido, Object payload) {
        EventoRealtime evento = EventoRealtime.criar(
                tipo,
                pedido.id(),
                pedido.idMesa(),
                pedido.idAtendimento(),
                idGarcom(pedido),
                payload
        );

        Set<String> canais = canaisGestao();
        canais.add(CanalRealtime.COZINHA);
        adicionarSePresente(canais, pedido.idMesa(), CanalRealtime::mesa);

        if (CANAL_GARCOM.equalsIgnoreCase(pedido.canal())) {
            adicionarSePresente(canais, pedido.idUsuario(), CanalRealtime::garcom);
        }

        if (CANAL_TOTEM.equalsIgnoreCase(pedido.canal())) {
            adicionarSePresente(canais, pedido.idUsuario(), CanalRealtime::totem);
            if (TipoEventoRealtime.PEDIDO_PRONTO.equals(tipo)) {
                canais.add(CanalRealtime.BALCAO);
            }
        }

        publicar(canais, evento);
    }

    private Set<String> canaisGestao() {
        return new LinkedHashSet<>(Set.of(
                CanalRealtime.GESTOR,
                CanalRealtime.ADMIN
        ));
    }

    private Integer idGarcom(PedidoResponse pedido) {
        return CANAL_GARCOM.equalsIgnoreCase(pedido.canal()) ? pedido.idUsuario() : null;
    }

    private void adicionarSePresente(
            Set<String> canais,
            Integer id,
            java.util.function.Function<Integer, String> canalFactory
    ) {
        if (id != null && id > 0) {
            canais.add(canalFactory.apply(id));
        }
    }

    private void publicar(Set<String> canais, EventoRealtime evento) {
        canais.forEach(canal -> emitterRegistry.publicar(canal, evento));
    }
}
