package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import java.util.List;

public record CriarPedidoRequest(
        Integer id,
        Integer codigo,
        String canal,
        String status,
        Integer idMesa,
        Integer idUsuario,
        Integer idAtendimento,
        List<ProdutoPedidoRequest> itens
) {
}
