package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Pedido ativo exibido na fila da cozinha.")
public record PedidoFilaCozinhaResponse(
        @Schema(description = "Identificador do pedido", example = "25")
        Integer id,

        @Schema(description = "Codigo visivel do pedido", example = "100025")
        Integer codigo,

        @Schema(description = "Canal em que o pedido foi enviado", example = "MESA")
        String canal,

        @Schema(description = "Status atual do pedido na cozinha", example = "ENVIADO_COZINHA")
        String status,

        @Schema(description = "Mesa vinculada ao pedido, quando houver", example = "1")
        Integer idMesa,

        @Schema(description = "Origem operacional resolvida do pedido", example = "Mesa 02")
        String origem,

        @Schema(description = "Atendimento/sessao da mesa vinculado ao pedido, quando houver", example = "8")
        Integer idAtendimento,

        @Schema(description = "Data e hora de criacao do pedido, usada para ordenar a fila por chegada", example = "2026-07-02T10:30:00")
        LocalDateTime dataCriacao,

        @Schema(description = "Data e hora em que o preparo foi iniciado", example = "2026-07-02T10:36:00")
        LocalDateTime dataInicioPreparo,

        @Schema(description = "Data e hora em que o pedido ficou pronto", example = "2026-07-02T10:52:00")
        LocalDateTime dataPronto,

        @Schema(description = "Itens do pedido com observacoes individuais")
        List<ItemFilaCozinhaResponse> itens
) {
}
