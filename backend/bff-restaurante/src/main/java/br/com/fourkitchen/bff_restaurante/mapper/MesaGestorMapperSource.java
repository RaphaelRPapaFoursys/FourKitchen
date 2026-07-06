package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoGestorResponse;

import java.util.List;

public record MesaGestorMapperSource(
        MesaClientResponse mesa,
        String garcomNome,
        List<PedidoGestorResponse> pedidos
) {
}
