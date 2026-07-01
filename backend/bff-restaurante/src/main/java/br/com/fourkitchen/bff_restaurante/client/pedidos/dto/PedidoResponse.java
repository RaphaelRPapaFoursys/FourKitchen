package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

public record PedidoResponse(
        Integer id,
        Integer codigo,
        String canal,
        String status,
        Integer idMesa,
        Integer idUsuario,
        Integer idAtendimento
) {
}
