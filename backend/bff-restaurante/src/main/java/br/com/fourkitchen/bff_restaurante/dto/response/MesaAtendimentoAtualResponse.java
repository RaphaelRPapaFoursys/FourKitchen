package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados do atendimento atual da mesa autenticada.")
public record MesaAtendimentoAtualResponse(
        @Schema(description = "Mesa vinculada ao usuario autenticado", example = "1")
        Integer idMesa,

        @Schema(description = "Atendimento aberto da mesa", example = "8")
        Integer idAtendimento,

        @Schema(description = "Codigo do atendimento usado pelo frontend da mesa", example = "123456")
        Integer codigoAtendimento,

        @Schema(description = "Status atual da mesa", example = "OCUPADA")
        String status
) {
}
