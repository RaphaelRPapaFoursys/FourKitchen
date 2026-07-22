package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResumoTotemClientResponse(
        Integer idUsuario,
        Long pedidosHoje,
        BigDecimal valorHoje,
        LocalDateTime ultimaAtividade,
        Long problemasAbertos
) {
}
