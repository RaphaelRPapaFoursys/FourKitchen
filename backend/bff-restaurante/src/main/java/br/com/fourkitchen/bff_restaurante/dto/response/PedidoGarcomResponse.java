package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pedido criado pelo garcom e enviado para a cozinha.")
public record PedidoGarcomResponse(
        @Schema(description = "Identificador do pedido", example = "25")
        Integer id,

        @Schema(description = "Codigo visivel do pedido", example = "100025")
        Integer codigo,

        @Schema(description = "Canal de origem do pedido", example = "GARCOM")
        String canal,

        @Schema(description = "Status inicial do pedido", example = "ENVIADO_COZINHA")
        String status,

        @Schema(description = "Mesa vinculada ao pedido", example = "1")
        Integer idMesa,

        @Schema(description = "Garcom autenticado que criou o pedido", example = "7")
        Integer idGarcom,

        @Schema(description = "Atendimento/sessao da mesa vinculado ao pedido", example = "8")
        Integer idAtendimento
) {
}
