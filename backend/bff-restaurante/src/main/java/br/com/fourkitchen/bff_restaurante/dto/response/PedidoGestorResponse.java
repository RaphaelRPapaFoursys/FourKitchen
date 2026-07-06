package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Pedido ativo de uma mesa exibida no painel do gestor.")
public record PedidoGestorResponse(
        @Schema(description = "Identificador do pedido", example = "42")
        Integer id,

        @Schema(description = "Status atual do pedido", example = "PRONTO")
        String status,

        @Schema(description = "Valor total do pedido (soma de quantidade x preco unitario dos itens)", example = "89.50")
        BigDecimal valor,

        @Schema(description = "Data de criacao do pedido", example = "2026-07-02T10:15:00")
        LocalDateTime criadoEm,

        @Schema(description = "Quantidade total de itens do pedido", example = "3")
        Integer totalItens
) {
}
