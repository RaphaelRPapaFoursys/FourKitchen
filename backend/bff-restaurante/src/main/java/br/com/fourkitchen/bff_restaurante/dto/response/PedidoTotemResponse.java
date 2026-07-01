package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pedido criado pelo totem e devolvido ao cliente.")
public record PedidoTotemResponse(
        @Schema(description = "Identificador do pedido", example = "25")
        Integer id,

        @Schema(description = "Codigo visivel do pedido", example = "100025")
        Integer codigo,

        @Schema(description = "Canal de origem do pedido", example = "TOTEM")
        String canal,

        @Schema(description = "Status inicial do pedido", example = "ENVIADO_COZINHA")
        String status
) {
}
