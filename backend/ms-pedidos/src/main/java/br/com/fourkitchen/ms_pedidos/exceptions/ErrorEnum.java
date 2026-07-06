package br.com.fourkitchen.ms_pedidos.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorEnum {

    PEDIDO_NAO_ENCONTRADO(
            "001",
            "Pedido nao encontrado",
            HttpStatus.NOT_FOUND
    ),

    TRANSICAO_STATUS_INVALIDA(
            "002",
            "Transicao de status invalida",
            HttpStatus.BAD_REQUEST
    ),

    DADOS_INVALIDOS(
            "003",
            "Dados invalidos",
            HttpStatus.BAD_REQUEST
    ),

    PEDIDO_AGUARDANDO_DECISAO(
            "004",
            "Produto esta aguardando decisão",
            HttpStatus.BAD_REQUEST
    ),
    PEDIDO_NAO_PODE_SINALIZAR_PROBLEMA(
            "005",
            "Status do pedido não permite sinalizar problema",
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

    ErrorEnum(String errorCode, String errorMessage, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }
}
