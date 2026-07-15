package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Resumo financeiro do atendimento atual da mesa.")
public record ResumoContaMesaResponse(
        @Schema(description = "Identificador do atendimento atual", example = "8")
        Integer idAtendimento,

        @Schema(description = "Codigo do atendimento usado pelo tablet da mesa", example = "123456")
        Integer codigoAtendimento,

        @Schema(description = "Valor final de todos os pedidos cobrados no atendimento", example = "149.70")
        BigDecimal valorFinal,

        @Schema(description = "Quantidade de pedidos considerados", example = "3")
        Integer totalPedidos,

        @Schema(description = "Quantidade total de itens considerados", example = "7")
        Integer totalItens
) {
}
