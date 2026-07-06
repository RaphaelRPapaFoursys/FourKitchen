package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

public record ResumoPedidosOperacaoResponse(
        Long pedidosEmPreparo,
        Long pedidosProntos,
        Long problemasPendentes
) {
}
