package br.com.fourkitchen.bff_restaurante.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status do item/produto do pedido quando a cozinha sinaliza um problema.")
public enum StatusProdutoPedido {
    DISPONIVEL,
    FALTA_PRODUTO,
    ERRO,
    INDISPONIVEL
}
