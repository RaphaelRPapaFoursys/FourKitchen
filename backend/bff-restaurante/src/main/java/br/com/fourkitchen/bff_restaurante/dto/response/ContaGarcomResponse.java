package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Resumo financeiro da conta da mesa.")
public record ContaGarcomResponse(
        @Schema(description = "Subtotal da conta", example = "149.70")
        BigDecimal subtotal,

        @Schema(description = "Total da conta", example = "149.70")
        BigDecimal total,

        @Schema(description = "Quantidade de pedidos considerados", example = "3")
        Integer totalPedidos,

        @Schema(description = "Quantidade total de itens considerados", example = "7")
        Integer totalItens
) {
}
