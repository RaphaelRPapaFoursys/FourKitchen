package br.com.fourkitchen.ms_pagamentos.dto.response;

import br.com.fourkitchen.ms_pagamentos.enums.StatusPagamento;

public record PagamentoResponse(
        StatusPagamento status,
        String mensagem,
        String codigoAutorizacao
) {
}
