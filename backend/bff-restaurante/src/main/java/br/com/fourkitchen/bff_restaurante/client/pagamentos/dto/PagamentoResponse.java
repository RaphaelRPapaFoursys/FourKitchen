package br.com.fourkitchen.bff_restaurante.client.pagamentos.dto;

public record PagamentoResponse(
        String status,
        String mensagem,
        String codigoAutorizacao
) {
}
