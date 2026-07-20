package br.com.fourkitchen.ms_pedidos.repository.projection;

import java.time.LocalDateTime;

public interface VolumeHorarioProjection {
    LocalDateTime getHorario();
    Long getQuantidade();
}
