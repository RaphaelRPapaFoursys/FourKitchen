package br.com.fourkitchen.bff_restaurante.realtime;

import java.time.LocalDateTime;

public record EventoRealtime(
        TipoEventoRealtime tipo,
        Integer idPedido,
        Integer idMesa,
        Integer idAtendimento,
        Integer idGarcom,
        LocalDateTime data,
        Object payload
) {
    public static EventoRealtime criar(
            TipoEventoRealtime tipo,
            Integer idPedido,
            Integer idMesa,
            Integer idAtendimento,
            Integer idGarcom,
            Object payload
    ) {
        return new EventoRealtime(
                tipo,
                idPedido,
                idMesa,
                idAtendimento,
                idGarcom,
                LocalDateTime.now(),
                payload
        );
    }
}
