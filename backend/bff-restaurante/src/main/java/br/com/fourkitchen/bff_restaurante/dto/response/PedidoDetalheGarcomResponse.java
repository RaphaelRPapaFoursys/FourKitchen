package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Pedido detalhado do atendimento atual exibido ao garcom.")
public record PedidoDetalheGarcomResponse(
        @Schema(description = "Identificador do pedido", example = "25")
        Integer id,

        @Schema(description = "Codigo visivel do pedido", example = "100025")
        Integer codigo,

        @Schema(description = "Canal em que o pedido foi criado", example = "GARCOM")
        String canal,

        @Schema(description = "Status atual do pedido", example = "EM_PREPARO")
        String status,

        @Schema(description = "Data de criacao do pedido", example = "2026-07-02T10:30:00")
        LocalDateTime dataCriacao,

        @Schema(description = "Data de inicio do preparo", example = "2026-07-02T10:36:00")
        LocalDateTime dataInicioPreparo,

        @Schema(description = "Data em que o pedido ficou pronto", example = "2026-07-02T10:52:00")
        LocalDateTime dataPronto,

        @Schema(description = "Itens do pedido")
        List<ItemPedidoDetalheGarcomResponse> itens
) {
}
