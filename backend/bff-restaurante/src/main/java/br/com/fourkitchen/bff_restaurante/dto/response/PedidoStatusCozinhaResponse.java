package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pedido com status atualizado pela cozinha.")
public record PedidoStatusCozinhaResponse(
        @Schema(description = "Identificador do pedido", example = "25")
        Integer id,

        @Schema(description = "Codigo visivel do pedido", example = "100025")
        Integer codigo,

        @Schema(description = "Canal de origem do pedido", example = "GARCOM")
        String canal,

        @Schema(description = "Status atualizado do pedido", example = "EM_PREPARO")
        String status,

        @Schema(description = "Mesa vinculada ao pedido, quando houver", example = "1")
        Integer idMesa,

        @Schema(description = "Atendimento/sessao da mesa vinculado ao pedido, quando houver", example = "8")
        Integer idAtendimento
) {
}
