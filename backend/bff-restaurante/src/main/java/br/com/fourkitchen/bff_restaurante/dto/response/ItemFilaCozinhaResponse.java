package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Item de um pedido exibido na fila da cozinha.")
public record ItemFilaCozinhaResponse(
        @Schema(description = "Identificador do item do pedido", example = "5")
        Integer id,

        @Schema(description = "Identificador do produto solicitado", example = "10")
        Integer idProduto,

        @Schema(description = "Nome do produto registrado no momento do pedido", example = "X-Burger")
        String nomeProduto,

        @Schema(description = "Quantidade solicitada do produto", example = "2")
        Integer quantidade,

        @Schema(description = "Preco unitario registrado no momento do pedido", example = "29.90")
        BigDecimal precoUnitario,

        @Schema(description = "Observacao especifica do item", example = "Sem cebola")
        String observacao,

        @Schema(description = "Status atual do item no pedido", example = "DISPONIVEL")
        String status
) {
}
