package br.com.fourkitchen.bff_restaurante.dto.response;

import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Notificacao retornada pelo BFF para os frontends.")
public record NotificacaoResponse(
        @Schema(description = "Identificador da notificacao", example = "1")
        Integer id,

        @Schema(
                description = "Tipo da notificacao",
                example = "PEDIDO_PRONTO",
                allowableValues = {
                        "PEDIDO_EM_PREPARO",
                        "PEDIDO_PRONTO",
                        "PEDIDO_COM_FALTA",
                        "PEDIDO_CANCELADO",
                        "CHAMADA_GARCOM",
                        "CONTA_SOLICITADA",
                        "ALTERACAO_PEDIDO_SOLICITADA"
                }
        )
        String tipo,

        @Schema(description = "Mensagem da notificacao", example = "Pedido pronto para retirada")
        String mensagem,

        @Schema(description = "Destino da notificacao", example = "COZINHA")
        DestinoNotificacao destino,

        @Schema(description = "Indica se a notificacao ja foi lida", example = "false")
        Boolean lida,

        @Schema(description = "Data e hora de criacao da notificacao", example = "2026-07-01T13:25:09")
        LocalDateTime data,

        @Schema(description = "Mesa relacionada a notificacao, quando houver", example = "1")
        Integer idMesa,

        @Schema(description = "Atendimento relacionado a notificacao, quando houver", example = "8")
        Integer idAtendimento,

        @Schema(description = "Garcom relacionado a notificacao, quando houver", example = "7")
        Integer idGarcom
) {
}
