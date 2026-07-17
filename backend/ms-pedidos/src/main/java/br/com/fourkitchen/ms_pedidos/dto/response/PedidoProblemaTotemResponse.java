package br.com.fourkitchen.ms_pedidos.dto.response;

import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoProblemaTotemResponse(
        Integer id,
        Integer codigo,
        StatusPedido status,
        LocalDateTime dataCriacao,
        Integer idGarcomResponsavelProblema,
        List<ItemPedidoCozinhaResponse> itens
) {
}
