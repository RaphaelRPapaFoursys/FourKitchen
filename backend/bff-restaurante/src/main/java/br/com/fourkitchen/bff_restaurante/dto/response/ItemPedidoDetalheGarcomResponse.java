package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Item detalhado de pedido exibido ao garcom.")
public record ItemPedidoDetalheGarcomResponse(
        @Schema(description = "Identificador do item do pedido", example = "5")
        Integer id,

        @Schema(description = "Identificador do produto", example = "10")
        Integer idProduto,

        @Schema(description = "Nome do produto registrado no pedido", example = "X-Burger")
        String nomeProduto,

        @Schema(description = "Quantidade solicitada", example = "2")
        Integer quantidade,

        @Schema(description = "Preco unitario registrado no pedido", example = "29.90")
        BigDecimal precoUnitario,

        @Schema(description = "Observacao do item", example = "Sem cebola")
        String observacao,

        @Schema(description = "Status atual do item", example = "DISPONIVEL")
        String status
) {
}
