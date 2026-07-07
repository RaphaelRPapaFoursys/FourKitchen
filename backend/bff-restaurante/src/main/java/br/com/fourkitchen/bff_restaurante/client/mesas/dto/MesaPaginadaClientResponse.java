package br.com.fourkitchen.bff_restaurante.client.mesas.dto;

import java.util.List;

public record MesaPaginadaClientResponse(
        List<MesaClientResponse> content,
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages,
        Boolean first,
        Boolean last
) {
}
