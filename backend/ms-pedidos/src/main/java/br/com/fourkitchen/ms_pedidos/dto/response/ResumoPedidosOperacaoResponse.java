package br.com.fourkitchen.ms_pedidos.dto.response;

public record ResumoPedidosOperacaoResponse(
        Long pedidosEmPreparo,
        Long pedidosProntos,
        Long problemasPendentes
) {
}
