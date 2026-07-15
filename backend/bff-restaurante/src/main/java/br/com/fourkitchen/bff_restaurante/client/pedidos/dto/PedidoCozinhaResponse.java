package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoCozinhaResponse(
        Integer id,
        Integer codigo,
        String canal,
        String status,
        Integer idMesa,
        Integer idUsuario,
        Integer idAtendimento,
        LocalDateTime dataCriacao,
        LocalDateTime dataInicioPreparo,
        LocalDateTime dataPronto,
        List<ItemPedidoCozinhaResponse> itens
) {
}
