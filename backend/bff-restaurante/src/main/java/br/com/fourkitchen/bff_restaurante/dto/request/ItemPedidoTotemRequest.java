package br.com.fourkitchen.bff_restaurante.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Item do pedido criado pelo totem. O preco unitario e consultado no ms-produtos.")
public record ItemPedidoTotemRequest(
        @Schema(description = "Identificador do produto escolhido", example = "10")
        @NotNull(message = "O campo idProduto nao pode ser nulo")
        @Positive(message = "O campo idProduto deve ser positivo")
        Integer idProduto,

        @Schema(description = "Quantidade solicitada do produto", example = "2")
        @NotNull(message = "O campo quantidade nao pode ser nulo")
        @Positive(message = "O campo quantidade deve ser positiva")
        Integer quantidade,

        @Schema(description = "Observacao opcional do cliente", example = "Sem cebola")
        String observacao
) {
}
