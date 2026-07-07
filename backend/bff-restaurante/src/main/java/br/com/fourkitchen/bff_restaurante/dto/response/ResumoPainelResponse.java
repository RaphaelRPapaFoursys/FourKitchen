package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Resumo dos KPIs exibidos no painel do gestor.")
public record ResumoPainelResponse(
        @Schema(description = "Quantidade de mesas livres", example = "12")
        Integer mesasLivres,

        @Schema(description = "Quantidade de mesas com pedidos em preparo", example = "5")
        Integer emPreparo,

        @Schema(description = "Quantidade de mesas com pedidos prontos para entrega", example = "3")
        Integer prontos,

        @Schema(description = "Quantidade de mesas em criticidade critica", example = "2")
        Integer problemas,

        @Schema(description = "Ticket medio dos atendimentos ativos", example = "82.50", nullable = true)
        BigDecimal ticketMedio,

        @Schema(description = "Carga de mesas ativas por garcom")
        List<CargaGarcomResponse> cargaGarcons
) {
}
