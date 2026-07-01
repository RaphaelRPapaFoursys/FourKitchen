package br.com.fourkitchen.bff_restaurante.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Dados enviados pelo totem para criar um pedido. O preco dos produtos e definido pelo ms-produtos.")
public record CriarPedidoTotemRequest(
        @Schema(description = "Itens escolhidos pelo cliente no totem, sem preco unitario")
        @Valid
        @NotEmpty(message = "O pedido deve possuir ao menos um item")
        List<ItemPedidoTotemRequest> itens
) {
}
