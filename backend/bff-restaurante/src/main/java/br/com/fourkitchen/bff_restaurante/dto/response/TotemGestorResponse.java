package br.com.fourkitchen.bff_restaurante.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TotemGestorResponse(
        Integer id,
        String nome,
        String email,
        Boolean ativo,
        Long pedidosHoje,
        BigDecimal valorHoje,
        LocalDateTime ultimaAtividade,
        Long problemasAbertos
) {
}
