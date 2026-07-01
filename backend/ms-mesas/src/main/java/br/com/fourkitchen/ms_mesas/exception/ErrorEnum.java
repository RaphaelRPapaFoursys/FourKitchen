package br.com.fourkitchen.ms_mesas.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorEnum {

    MESA_NAO_ENCONTRADA(
            "001",
            "Mesa nao encontrada",
            HttpStatus.NOT_FOUND
    ),

    NUMERO_MESA_JA_CADASTRADO(
            "002",
            "Numero da mesa ja cadastrado",
            HttpStatus.CONFLICT
    ),

    MESA_NAO_DISPONIVEL(
            "003",
            "Mesa nao esta disponivel para abertura",
            HttpStatus.BAD_REQUEST
    ),

    MESA_NAO_OCUPADA(
            "004",
            "Mesa nao esta ocupada para fechamento",
            HttpStatus.BAD_REQUEST
    ),

    MESA_COM_PEDIDOS_ATIVOS(
            "005",
            "Mesa possui pedidos ativos",
            HttpStatus.BAD_REQUEST
    ),

    GARCOM_INVALIDO(
            "006",
            "Garcom invalido",
            HttpStatus.BAD_REQUEST
    ),

    ATENDIMENTO_NAO_ABERTO(
            "007",
            "Mesa nao possui atendimento aberto",
            HttpStatus.BAD_REQUEST
    ),

    DADOS_INVALIDOS(
            "008",
            "Dados invalidos",
            HttpStatus.BAD_REQUEST
    ),

    CODIGO_SESSAO_INVALIDO(
            "009",
            "Codigo de sessao invalido",
            HttpStatus.BAD_REQUEST
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
