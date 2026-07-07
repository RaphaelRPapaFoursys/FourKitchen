package br.com.fourkitchen.ms_mesas.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record MesaPaginadaResponse(
        List<MesaResponse> content,
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages,
        Boolean first,
        Boolean last
) {

    public static MesaPaginadaResponse from(Page<MesaResponse> page) {
        return new MesaPaginadaResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
