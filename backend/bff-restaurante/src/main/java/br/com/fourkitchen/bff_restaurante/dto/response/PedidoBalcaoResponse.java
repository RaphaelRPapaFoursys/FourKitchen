package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Pedido do totem acompanhado pela operacao do balcao.")
public record PedidoBalcaoResponse(
        Integer id,
        Integer codigo,
        String status,
        LocalDateTime dataCriacao,
        LocalDateTime dataInicioPreparo,
        LocalDateTime dataPronto
) {
}
