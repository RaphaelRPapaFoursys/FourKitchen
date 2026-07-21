package br.com.fourkitchen.ms_pedidos.dto.response;

import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;

import java.time.LocalDateTime;

public record PedidoRetiradaResponse(
        Integer id,
        Integer codigo,
        StatusPedido status,
        LocalDateTime dataCriacao,
        LocalDateTime dataInicioPreparo,
        LocalDateTime dataPronto
) {
}
