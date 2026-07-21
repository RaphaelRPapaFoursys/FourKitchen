package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import java.time.LocalDateTime;

public record PedidoRetiradaResponse(
        Integer id,
        Integer codigo,
        String status,
        LocalDateTime dataCriacao,
        LocalDateTime dataInicioPreparo,
        LocalDateTime dataPronto
) {
}
