package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dados essenciais para o garcom resolver problemas sinalizados pela cozinha.")
public record MesaProblemasGarcomResponse(
        @Schema(description = "Mesa atribuida ao garcom autenticado")
        MesaDetalheGarcomResponse mesa,

        @Schema(description = "Pedidos da mesa que aguardam decisao")
        List<PedidoDetalheGarcomResponse> pedidos,

        @Schema(description = "Itens com problemas pendentes de decisao")
        List<ProblemaPedidoGarcomResponse> problemas
) {
}
