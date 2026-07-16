package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import br.com.fourkitchen.bff_restaurante.enums.StatusProdutoPedido;
import io.swagger.v3.oas.annotations.media.Schema;

public record SinalizarProblemaResponse(
        Integer idPedido,
        Integer idProdutoPedido,
        String statusPedido,
        StatusProdutoPedido statusProdutoPedido,
        Integer idMesa,
        Integer idAtendimento
) {
}
