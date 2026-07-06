package br.com.fourkitchen.ms_notificacoes.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorEnum {

    NOTIFICACAO_NAO_ENCONTRADA(
            "001",
            "Notificacao nao encontrada",
            HttpStatus.NOT_FOUND
    ),

    DADOS_INVALIDOS(
            "002",
            "Dados invalidos",
            HttpStatus.BAD_REQUEST
    ),

    CHAMADA_GARCOM_INVALIDA(
            "003",
            "Chamada de garcom invalida",
            HttpStatus.BAD_REQUEST
    ),

    CHAMADA_GARCOM_NAO_PERTENCE_AO_GARCOM(
            "004",
            "Chamada de garcom nao pertence ao garcom",
            HttpStatus.FORBIDDEN
    ),

    ERRO_INTERNO(
            "500",
            "Erro interno do servidor",
            HttpStatus.INTERNAL_SERVER_ERROR
    );

    private final String errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;

    ErrorEnum(String errorCode, String errorMessage, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }
}
