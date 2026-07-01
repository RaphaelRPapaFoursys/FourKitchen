package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pedido criado pela mesa e devolvido ao tablet.")
public record PedidoMesaResponse(
        @Schema(description = "Identificador do pedido", example = "25")
        Integer id,

        @Schema(description = "Codigo visivel do pedido", example = "100025")
        Integer codigo,

        @Schema(description = "Canal de origem do pedido", example = "MESA")
        String canal,

        @Schema(description = "Status inicial do pedido", example = "ENVIADO_COZINHA")
        String status,

        @Schema(description = "Mesa vinculada ao pedido", example = "1")
        Integer idMesa,

        @Schema(description = "Atendimento/sessao da mesa vinculado ao pedido", example = "8")
        Integer idAtendimento
) {
}
