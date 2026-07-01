package br.com.fourkitchen.bff_restaurante.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorEnum {

    CREDENCIAIS_INVALIDAS(
            "001",
            "Credenciais invalidas",
            HttpStatus.UNAUTHORIZED
    ),

    TOKEN_INVALIDO(
            "002",
            "Token invalido ou expirado",
            HttpStatus.UNAUTHORIZED
    ),

    ACESSO_NEGADO(
            "003",
            "Acesso negado",
            HttpStatus.FORBIDDEN
    ),

    DADOS_INVALIDOS(
            "004",
            "Dados invalidos",
            HttpStatus.BAD_REQUEST
    ),

    MS_USUARIOS_INDISPONIVEL(
            "005",
            "Servico de usuarios indisponivel",
            HttpStatus.BAD_GATEWAY
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
