package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Mesa exibida no painel do gestor.")
public record MesaGestorResponse(
        @Schema(description = "Identificador da mesa", example = "1")
        Integer id,

        @Schema(description = "Numero fisico/visual da mesa", example = "10")
        Integer numero,

        @Schema(description = "Status atual da mesa", example = "OCUPADA")
        String status,

        @Schema(description = "Identificador do garcom atribuido", example = "7", nullable = true)
        Integer garcomId,

        @Schema(description = "Nome do garcom atribuido", example = "Amanda Souza", nullable = true)
        String garcomNome,

        @Schema(description = "Codigo da sessao aberta", example = "123456", nullable = true)
        Integer codigoSessao,

        @Schema(description = "Data de abertura da mesa", example = "2026-07-02T10:00:00", nullable = true)
        LocalDateTime dataAbertura,

        @Schema(description = "Data de fechamento da ultima sessao", example = "2026-07-02T12:00:00", nullable = true)
        LocalDateTime dataFechamento,

        @Schema(description = "Pedidos ativos do atendimento aberto na mesa")
        List<PedidoGestorResponse> pedidos
) {
}
