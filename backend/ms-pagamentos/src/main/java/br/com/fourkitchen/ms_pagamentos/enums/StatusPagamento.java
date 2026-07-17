package br.com.fourkitchen.ms_pagamentos.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StatusPagamento {
    APROVADO(
            "001",
            "Pagamento aprovado",
            HttpStatus.OK
    ),

    RECUSADO(
            "002",
            "Pagamento recusado",
            HttpStatus.PAYMENT_REQUIRED
    );

    private final String errorCode;

    private final String errorMessage;
    private final HttpStatus httpStatus;

    StatusPagamento(String errorCode, String errorMessage, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }
}
