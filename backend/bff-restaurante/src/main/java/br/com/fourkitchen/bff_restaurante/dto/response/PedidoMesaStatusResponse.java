package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Pedido do atendimento atual da mesa com status e itens.")
public record PedidoMesaStatusResponse(
        @Schema(description = "Identificador do pedido", example = "25")
        Integer id,

        @Schema(description = "Codigo visivel do pedido", example = "100025")
        Integer codigo,

        @Schema(description = "Canal de origem do pedido", example = "MESA", allowableValues = {"MESA", "TOTEM", "GARCOM"})
        String canal,

        @Schema(
                description = "Status atual do pedido",
                example = "ENVIADO_COZINHA",
                allowableValues = {
                        "ENVIADO_COZINHA",
                        "EM_PREPARO",
                        "PRONTO",
                        "ENTREGUE",
                        "FINALIZADO",
                        "CANCELADO",
                        "AGUARDANDO_DECISAO",
                        "PROBLEMA_COZINHA"
                }
        )
        String status,

        @Schema(description = "Mesa vinculada ao atendimento", example = "1", nullable = true)
        Integer idMesa,

        @Schema(description = "Atendimento/sessao da mesa vinculado ao pedido", example = "8", nullable = true)
        Integer idAtendimento,

        @Schema(description = "Codigo do atendimento usado pelo tablet da mesa", example = "123456", nullable = true)
        Integer codigoAtendimento,

        @Schema(description = "Data e hora de criacao do pedido", example = "2026-07-02T10:30:00", nullable = true)
        LocalDateTime dataCriacao,

        @Schema(description = "Itens do pedido")
        List<ItemPedidoMesaStatusResponse> itens
) {
}
