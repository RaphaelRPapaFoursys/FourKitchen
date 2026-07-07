package br.com.fourkitchen.bff_restaurante.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Item do pedido criado pelo tablet da mesa.")
public record ItemPedidoMesaRequest(
        @Schema(description = "Identificador do produto escolhido", example = "10")
        @NotNull(message = "O campo idProduto nao pode ser nulo")
        @Positive(message = "O campo idProduto deve ser positivo")
        Integer idProduto,

        @Schema(description = "Quantidade solicitada do produto", example = "2")
        @NotNull(message = "O campo quantidade nao pode ser nulo")
        @Positive(message = "O campo quantidade deve ser positiva")
        Integer quantidade,

        @Schema(description = "Preco unitario do produto no momento do pedido", example = "29.90")
        @NotNull(message = "O campo precoUnitario nao pode ser nulo")
        @DecimalMin(value = "0.01", message = "O campo precoUnitario deve ser maior que zero")
        BigDecimal precoUnitario,

        @Schema(description = "Observacao opcional do cliente", example = "Sem cebola")
        String observacao
) {
}
