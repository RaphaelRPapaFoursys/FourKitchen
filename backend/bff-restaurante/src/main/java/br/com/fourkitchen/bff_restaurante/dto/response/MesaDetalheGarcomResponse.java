package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Dados da mesa atribuida ao garcom no detalhe operacional.")
public record MesaDetalheGarcomResponse(
        @Schema(description = "Identificador da mesa", example = "1")
        Integer idMesa,

        @Schema(description = "Numero fisico/visual da mesa", example = "10")
        Integer numero,

        @Schema(description = "Status da mesa", example = "OCUPADA")
        String status,

        @Schema(description = "Atendimento aberto da mesa", example = "8")
        Integer idAtendimento,

        @Schema(description = "Codigo da sessao aberta para a mesa", example = "123456")
        Integer codigoSessao,

        @Schema(description = "Data de abertura do atendimento", example = "2026-07-02T10:00:00")
        LocalDateTime dataAbertura
) {
}
