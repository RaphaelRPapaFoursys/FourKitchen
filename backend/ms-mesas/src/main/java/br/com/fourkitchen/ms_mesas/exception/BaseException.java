package br.com.fourkitchen.ms_mesas.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final ErrorEnum errorEnum;

    public BaseException(ErrorEnum errorEnum) {
        super(errorEnum.getErrorMessage());
        this.errorEnum = errorEnum;
    }
}
