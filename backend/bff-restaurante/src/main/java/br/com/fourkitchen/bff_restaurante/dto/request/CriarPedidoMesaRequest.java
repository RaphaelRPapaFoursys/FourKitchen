package br.com.fourkitchen.bff_restaurante.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "Dados enviados pelo tablet da mesa para criar um pedido.")
public record CriarPedidoMesaRequest(
        @Schema(description = "Codigo da sessao aberta para a mesa", example = "123456")
        @NotNull(message = "O campo codigoSessao nao pode ser nulo")
        @Positive(message = "O campo codigoSessao deve ser positivo")
        Integer codigoSessao,

        @Schema(description = "Itens escolhidos pelo cliente na mesa")
        @Valid
        @NotEmpty(message = "O pedido deve possuir ao menos um item")
        List<ItemPedidoMesaRequest> itens
) {
}
