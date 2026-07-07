package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Historico de atendimento finalizado para acompanhamento do gestor.")
public record HistoricoAtendimentoResponse(
        @Schema(description = "Identificador do historico", example = "1")
        Integer id,

        @Schema(description = "Identificador do atendimento original", example = "8")
        Integer idAtendimento,

        @Schema(description = "Codigo da sessao da mesa", example = "123456")
        Integer codigoSessao,

        @Schema(description = "Identificador da mesa", example = "1")
        Integer idMesa,

        @Schema(description = "Numero fisico/visual da mesa", example = "10")
        Integer numeroMesa,

        @Schema(description = "Identificador do garcom responsavel", example = "7", nullable = true)
        Integer idGarcom,

        @Schema(description = "Nome do garcom responsavel", example = "Amanda Souza", nullable = true)
        String nomeGarcom,

        @Schema(description = "Valor final da conta", example = "149.70")
        BigDecimal valorFinal,

        @Schema(description = "Quantidade de pedidos considerados na conta", example = "3")
        Integer totalPedidos,

        @Schema(description = "Quantidade total de itens considerados na conta", example = "7")
        Integer totalItens,

        @Schema(description = "Horario de abertura do atendimento", example = "2026-07-02T10:00:00")
        LocalDateTime dataAbertura,

        @Schema(description = "Horario em que o atendimento foi finalizado", example = "2026-07-02T11:20:00")
        LocalDateTime dataFechamento,

        @Schema(description = "Duracao do atendimento em minutos", example = "80")
        Integer duracaoMinutos
) {
}
