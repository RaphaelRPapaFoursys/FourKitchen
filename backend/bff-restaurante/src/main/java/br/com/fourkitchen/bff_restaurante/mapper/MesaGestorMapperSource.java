package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaClientResponse;

public record MesaGestorMapperSource(
        MesaClientResponse mesa,
        String garcomNome
) {
}
