package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pedido ativo exibido no painel publico de retirada.")
public record PedidoPainelRetiradaResponse(
        @Schema(description = "Codigo publico do pedido", example = "100025")
        Integer codigo,

        @Schema(description = "Status atual do pedido", example = "EM_PREPARO")
        String status
) {
}
