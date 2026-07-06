package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import br.com.fourkitchen.bff_restaurante.enums.StatusProdutoPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SinalizarProblemaRequest(
        Integer idPedido,
        Integer idProdutoPedido,
        StatusProdutoPedido statusProdutoPedido
) {
}
