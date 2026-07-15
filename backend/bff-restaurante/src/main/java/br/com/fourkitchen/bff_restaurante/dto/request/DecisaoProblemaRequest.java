package br.com.fourkitchen.bff_restaurante.dto.request;

import br.com.fourkitchen.bff_restaurante.enums.StatusProdutoPedido;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DecisaoProblemaRequest(
        @NotNull
        Integer idPedido,

        @NotNull
        Integer idProdutoPedido,

        @NotNull
        StatusProdutoPedido novoStatusProdutoPedido,

        @NotNull
        Boolean pedidoCancelado,

        Integer idNovoProduto,

        @Size(max = 255, message = "A observacao do produto substituto deve ter no maximo 255 caracteres")
        String observacaoNovoProduto
) {
    public DecisaoProblemaRequest(
            Integer idPedido,
            Integer idProdutoPedido,
            StatusProdutoPedido novoStatusProdutoPedido,
            Boolean pedidoCancelado,
            Integer idNovoProduto
    ) {
        this(idPedido, idProdutoPedido, novoStatusProdutoPedido, pedidoCancelado, idNovoProduto, null);
    }
}
