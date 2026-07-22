package br.com.fourkitchen.ms_pedidos.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResumoTotemResponse(
        Integer idUsuario,
        Long pedidosHoje,
        BigDecimal valorHoje,
        LocalDateTime ultimaAtividade,
        Long problemasAbertos
) {
}
