package br.com.fourkitchen.ms_usuarios.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorEnum {

    EMAIL_JA_CADASTRADO(
            "001",
            "Email ja cadastrado",
            HttpStatus.CONFLICT
    ),

    CREDENCIAIS_INVALIDAS(
            "002",
            "Email ou senha invalidos",
            HttpStatus.UNAUTHORIZED
    ),

    USUARIO_INATIVO(
            "003",
            "Usuario inativo",
            HttpStatus.FORBIDDEN
    ),

    TOKEN_INVALIDO(
            "005",
            "Token invalido ou expirado",
            HttpStatus.UNAUTHORIZED
    ),

    DADOS_INVALIDOS(
            "004",
            "Dados invalidos",
            HttpStatus.BAD_REQUEST
    ),

    USUARIO_NAO_ENCONTRADO(
            "006",
            "Usuario nao encontrado",
            HttpStatus.NOT_FOUND
    ),

    USUARIO_JA_INATIVO(
            "007",
            "Usuario ja esta inativo",
            HttpStatus.BAD_REQUEST
    ),

    NAO_PODE_EXCLUIR_PROPRIO_USUARIO(
            "008",
            "Nao e permitido excluir o proprio usuario autenticado",
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

    ErrorEnum(
            String errorCode,
            String errorMessage,
            HttpStatus httpStatus
    ) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }
}
