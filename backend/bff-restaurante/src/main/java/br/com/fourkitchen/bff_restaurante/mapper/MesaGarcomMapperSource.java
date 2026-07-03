package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaGarcomClientResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;

import java.util.List;

public record MesaGarcomMapperSource(
        MesaGarcomClientResponse mesa,
        List<PedidoResponse> pedidosAtivos,
        List<NotificacaoResponse> chamadasPendentes
) {
}
