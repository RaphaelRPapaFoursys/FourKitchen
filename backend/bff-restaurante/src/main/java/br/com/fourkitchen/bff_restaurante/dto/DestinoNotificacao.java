package br.com.fourkitchen.bff_restaurante.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Destino da notificacao dentro do restaurante.")
public enum DestinoNotificacao {
    GARCOM,
    MESA,
    TOTEM,
    COZINHA,
    GESTOR
}
