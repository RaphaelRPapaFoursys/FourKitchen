package br.com.fourkitchen.ms_pagamentos.exception;

import br.com.fourkitchen.ms_pagamentos.enums.StatusPagamento;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final StatusPagamento errorEnum;

    public BaseException(StatusPagamento errorEnum) {
        super(errorEnum.getErrorMessage());
        this.errorEnum = errorEnum;
    }
}
