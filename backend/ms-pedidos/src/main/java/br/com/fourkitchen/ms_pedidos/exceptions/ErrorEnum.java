package br.com.fourkitchen.ms_pedidos.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorEnum {

    PEDIDO_NAO_ENCONTRADO(
            "001",
            "Pedido nao encontrado.",
            HttpStatus.NOT_FOUND
    ),

    TRANSICAO_STATUS_INVALIDA(
            "002",
            "Transicao de status invalida.",
            HttpStatus.BAD_REQUEST
    ),

    DADOS_INVALIDOS(
            "003",
            "Dados invalidos.",
            HttpStatus.BAD_REQUEST
    ),

    RECURSO_NAO_ENCONTRADO(
            "404",
            "Recurso nao encontrado",
            HttpStatus.NOT_FOUND
    ),

    PEDIDO_AGUARDANDO_DECISAO(
            "004",
            "Produto esta aguardando decisão.",
            HttpStatus.BAD_REQUEST
    ),

    PEDIDO_NAO_PERMITE_DECISAO(
            "021",
            "O pedido deve estar com status AGUARDANDO_DECISAO ou PROBLEMA_COZINHA para receber uma decisão.",
            HttpStatus.BAD_REQUEST
    ),
      
    PEDIDO_NAO_PODE_SINALIZAR_PROBLEMA(
            "005",
            "Status do pedido não permite sinalizar problema",
            HttpStatus.BAD_REQUEST
    ),

    PEDIDO_ENCERRADO(
            "006",
            "Pedido encerrado.",
            HttpStatus.BAD_REQUEST
    ),

    PRODUTO_PEDIDO_NAO_ENCONTRADO(
            "007",
            "ProdutoPedido não encontrado.",
            HttpStatus.NOT_FOUND
    ),

    ERRO_INTERNO(
            "500",
            "Erro interno do servidor.",
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
