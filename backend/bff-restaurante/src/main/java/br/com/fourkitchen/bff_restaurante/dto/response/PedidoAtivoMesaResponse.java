package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pedido ativo vinculado a uma mesa do garcom.")
public record PedidoAtivoMesaResponse(
        @Schema(description = "Identificador do pedido", example = "25")
        Integer id,

        @Schema(description = "Codigo visivel do pedido", example = "100025")
        Integer codigo,

        @Schema(description = "Canal de origem do pedido", example = "GARCOM")
        String canal,

        @Schema(description = "Status atual do pedido", example = "ENVIADO_COZINHA")
        String status,

        @Schema(description = "Atendimento/sessao vinculado ao pedido", example = "8")
        Integer idAtendimento
) {
}
