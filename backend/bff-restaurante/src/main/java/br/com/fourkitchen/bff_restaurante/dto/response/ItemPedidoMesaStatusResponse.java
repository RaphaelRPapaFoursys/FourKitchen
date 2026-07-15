package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Item de um pedido do atendimento atual da mesa.")
public record ItemPedidoMesaStatusResponse(
        @Schema(description = "Identificador do produto solicitado", example = "10")
        Integer idProduto,

        @Schema(description = "Nome do produto, quando disponivel para exibicao", example = "X-Burger", nullable = true)
        String nome,

        @Schema(description = "Quantidade solicitada do produto", example = "2")
        Integer quantidade,

        @Schema(description = "Preco unitario registrado no momento do pedido", example = "29.90", nullable = true)
        BigDecimal precoUnitario,

        @Schema(description = "Valor total do item considerando preco e quantidade", example = "59.80")
        BigDecimal valorTotal,

        @Schema(description = "Observacao especifica do item", example = "Sem cebola", nullable = true)
        String observacao
) {
}
