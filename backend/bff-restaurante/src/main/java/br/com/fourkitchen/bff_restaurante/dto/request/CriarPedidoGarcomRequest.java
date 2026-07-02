package br.com.fourkitchen.bff_restaurante.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "Dados enviados pelo painel do garcom para criar um pedido.")
public record CriarPedidoGarcomRequest(
        @Schema(description = "Identificador da mesa atendida pelo garcom autenticado", example = "1")
        @NotNull(message = "O campo idMesa nao pode ser nulo")
        @Positive(message = "O campo idMesa deve ser positivo")
        Integer idMesa,

        @Schema(description = "Itens escolhidos pelo cliente e lancados pelo garcom")
        @Valid
        @NotEmpty(message = "O pedido deve possuir ao menos um item")
        List<ItemPedidoGarcomRequest> itens
) {
}
