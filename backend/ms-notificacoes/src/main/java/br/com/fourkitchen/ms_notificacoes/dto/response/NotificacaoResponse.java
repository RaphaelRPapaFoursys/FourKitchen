package br.com.fourkitchen.ms_notificacoes.dto.response;

import br.com.fourkitchen.ms_notificacoes.enums.DestinoNotificacao;

import java.time.LocalDateTime;

public record NotificacaoResponse(
        Integer id,
        String tipo,
        String mensagem,
        DestinoNotificacao destino,
        Boolean lida,
        LocalDateTime data,
        Integer idMesa,
        Integer idAtendimento,
        Integer idGarcom
) {
}
