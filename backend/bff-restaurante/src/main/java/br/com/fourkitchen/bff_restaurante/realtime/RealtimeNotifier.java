package br.com.fourkitchen.bff_restaurante.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RealtimeNotifier {

    private final RealtimePublisher publisher;

    public void pedidoAlterado(
            RealtimeEventType type,
            Integer idPedido,
            Integer idMesa,
            Integer idAtendimento,
            String status
    ) {
        Map<String, Object> data = new HashMap<>();
        putIfNotNull(data, "idPedido", idPedido);
        putIfNotNull(data, "idMesa", idMesa);
        putIfNotNull(data, "idAtendimento", idAtendimento);
        putIfNotNull(data, "status", status);

        List<String> destinations = new ArrayList<>(List.of(
                RealtimeDestination.COZINHA_PEDIDOS,
                RealtimeDestination.GARCOM_OPERACAO,
                RealtimeDestination.GESTOR_OPERACAO
        ));
        if (idMesa != null) {
            destinations.add(RealtimeDestination.mesa(idMesa));
        }

        publisher.publish(
                type,
                idPedido,
                data,
                destinations.toArray(String[]::new)
        );
    }

    public void mesaAlterada(
            RealtimeEventType type,
            Integer idMesa,
            Integer idAtendimento,
            Integer idGarcom
    ) {
        Map<String, Object> data = new HashMap<>();
        putIfNotNull(data, "idMesa", idMesa);
        putIfNotNull(data, "idAtendimento", idAtendimento);
        putIfNotNull(data, "idGarcom", idGarcom);

        publisher.publish(
                type,
                idMesa,
                data,
                RealtimeDestination.GARCOM_OPERACAO,
                RealtimeDestination.GESTOR_OPERACAO,
                idMesa == null ? null : RealtimeDestination.mesa(idMesa)
        );
    }

    public void chamadaGarcomAlterada(
            RealtimeEventType type,
            Integer idNotificacao,
            Integer idMesa,
            Integer idAtendimento,
            Integer idGarcom
    ) {
        Map<String, Object> data = new HashMap<>();
        putIfNotNull(data, "idNotificacao", idNotificacao);
        putIfNotNull(data, "idMesa", idMesa);
        putIfNotNull(data, "idAtendimento", idAtendimento);
        putIfNotNull(data, "idGarcom", idGarcom);

        publisher.publish(
                type,
                idNotificacao,
                data,
                RealtimeDestination.GARCOM_OPERACAO,
                RealtimeDestination.GESTOR_OPERACAO,
                idMesa == null ? null : RealtimeDestination.mesa(idMesa)
        );
    }

    public void catalogoAlterado(RealtimeEventType type, Integer id) {
        publisher.publish(
                type,
                id,
                Map.of("id", id),
                RealtimeDestination.CARDAPIO,
                RealtimeDestination.GESTOR_CATALOGO
        );
    }

    public void usuarioAlterado(Integer id) {
        publisher.publish(
                RealtimeEventType.USUARIO_ALTERADO,
                id,
                Map.of("idUsuario", id),
                RealtimeDestination.GESTOR_USUARIOS,
                RealtimeDestination.GESTOR_OPERACAO
        );
    }

    private void putIfNotNull(Map<String, Object> data, String key, Object value) {
        if (value != null) {
            data.put(key, value);
        }
    }
}
