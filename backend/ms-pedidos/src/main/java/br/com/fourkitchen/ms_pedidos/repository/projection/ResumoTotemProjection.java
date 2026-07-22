package br.com.fourkitchen.ms_pedidos.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ResumoTotemProjection {
    Integer getIdUsuario();

    Long getPedidosHoje();

    BigDecimal getValorHoje();

    LocalDateTime getUltimaAtividade();

    Long getProblemasAbertos();
}
