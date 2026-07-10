package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resposta do fechamento de conta realizado pelo garcom.")
public record FechamentoContaGarcomResponse(
        @Schema(description = "Identificador da mesa", example = "1")
        Integer idMesa,

        @Schema(description = "Numero da mesa", example = "10")
        Integer numero,

        @Schema(description = "Status final da mesa", example = "DISPONIVEL")
        String status,

        @Schema(description = "Atendimento finalizado", example = "8")
        Integer idAtendimento,

        @Schema(description = "Codigo da sessao finalizada", example = "123456")
        Integer codigoSessao,

        @Schema(description = "Data de abertura do atendimento", example = "2026-07-02T10:00:00")
        LocalDateTime dataAbertura,

        @Schema(description = "Data de fechamento retornada pelo servico de mesas", example = "2026-07-02T11:20:00")
        LocalDateTime dataFechamento,

        @Schema(description = "Resumo da conta fechada")
        ContaGarcomResponse conta
) {
}
