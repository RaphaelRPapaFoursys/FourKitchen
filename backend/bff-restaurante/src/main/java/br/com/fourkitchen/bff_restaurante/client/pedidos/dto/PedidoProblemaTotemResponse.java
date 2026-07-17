package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoProblemaTotemResponse(
        Integer id,
        Integer codigo,
        String status,
        LocalDateTime dataCriacao,
        Integer idGarcomResponsavelProblema,
        List<ItemPedidoCozinhaResponse> itens
) {
}
