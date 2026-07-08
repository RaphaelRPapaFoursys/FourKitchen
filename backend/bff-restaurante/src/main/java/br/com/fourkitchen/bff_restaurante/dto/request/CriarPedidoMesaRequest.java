package br.com.fourkitchen.bff_restaurante.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "Dados enviados pelo tablet da mesa para criar um pedido.")
public record CriarPedidoMesaRequest(
        @Schema(description = "Codigo do atendimento/sessao aberta para a mesa", example = "123456")
        @JsonAlias("codigoSessao")
        @NotNull(message = "O campo codigoAtendimento nao pode ser nulo")
        @Positive(message = "O campo codigoAtendimento deve ser positivo")
        Integer codigoAtendimento,

        @Schema(description = "Itens escolhidos pelo cliente na mesa")
        @Valid
        @NotEmpty(message = "O pedido deve possuir ao menos um item")
        List<ItemPedidoMesaRequest> itens
) {
    @Deprecated
    public Integer codigoSessao() {
        return codigoAtendimento;
    }
}
