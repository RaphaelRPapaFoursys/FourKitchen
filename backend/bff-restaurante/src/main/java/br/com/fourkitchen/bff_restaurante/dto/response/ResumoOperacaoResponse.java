package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo operacional para painel do gestor.")
public record ResumoOperacaoResponse(
        @Schema(description = "Total de pedidos em preparo.", example = "5")
        Long pedidosEmPreparo,

        @Schema(description = "Total de pedidos prontos.", example = "3")
        Long pedidosProntos,

        @Schema(description = "Total de mesas ocupadas.", example = "8")
        Long mesasOcupadas,

        @Schema(description = "Total de problemas pendentes de decisao.", example = "2")
        Long problemasPendentes,

        @Schema(description = "Total de chamadas de garcom pendentes.", example = "4")
        Long chamadasPendentes
) {
}
