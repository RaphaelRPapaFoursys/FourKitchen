package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Detalhe completo da mesa atribuida ao garcom.")
public record MesaGarcomDetalheResponse(
        @Schema(description = "Dados da mesa e atendimento")
        MesaDetalheGarcomResponse mesa,

        @Schema(description = "Resumo da conta")
        ContaGarcomResponse conta,

        @Schema(description = "Pedidos do atendimento atual")
        List<PedidoDetalheGarcomResponse> pedidos,

        @Schema(description = "Problemas pendentes de decisao do garcom")
        List<ProblemaPedidoGarcomResponse> problemas
) {
}
