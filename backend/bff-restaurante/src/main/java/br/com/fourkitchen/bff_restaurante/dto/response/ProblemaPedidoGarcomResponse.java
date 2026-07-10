package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Problema sinalizado pela cozinha para decisao do garcom.")
public record ProblemaPedidoGarcomResponse(
        @Schema(description = "Identificador do pedido com problema", example = "25")
        Integer idPedido,

        @Schema(description = "Identificador do item do pedido com problema", example = "5")
        Integer idProdutoPedido,

        @Schema(description = "Tipo/status do problema", example = "FALTA_PRODUTO")
        String tipo,

        @Schema(description = "Mensagem amigavel para exibicao", example = "Item com falta de produto")
        String mensagem
) {
}
