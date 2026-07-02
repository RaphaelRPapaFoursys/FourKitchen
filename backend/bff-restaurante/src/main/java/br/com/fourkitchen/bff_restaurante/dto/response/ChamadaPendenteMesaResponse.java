package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Chamada pendente vinculada a uma mesa do garcom.")
public record ChamadaPendenteMesaResponse(
        @Schema(description = "Identificador da notificacao/chamada", example = "3")
        Integer id,

        @Schema(description = "Tipo da chamada", example = "CHAMADA_GARCOM")
        String tipo,

        @Schema(description = "Mensagem exibida para o garcom", example = "Cliente solicitou atendimento")
        String mensagem,

        @Schema(description = "Data e hora em que a chamada foi criada", example = "2026-07-02T10:15:30")
        LocalDateTime data
) {
}
