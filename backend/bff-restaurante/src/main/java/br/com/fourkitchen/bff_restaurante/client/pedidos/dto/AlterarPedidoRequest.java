package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

public record AlterarPedidoRequest(
        String canal,
        String status,
        Integer idMesa,
        Integer idUsuario,
        Integer idAtendimento
) {
}
