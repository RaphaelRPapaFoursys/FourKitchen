package br.com.fourkitchen.bff_restaurante.dto.request;

import br.com.fourkitchen.bff_restaurante.enums.StatusProdutoPedido;
import jakarta.validation.constraints.NotNull;

public record DecisaoProblemaRequest(
        @NotNull
        Integer idPedido,

        @NotNull
        Integer idProdutoPedido,

        @NotNull
        StatusProdutoPedido novoStatusProdutoPedido,

        @NotNull
        Boolean pedidoCancelado,

        Integer idNovoProduto
) {
}
