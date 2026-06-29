package br.com.fourkitchen.ms_produtos.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorEnum {

    PRODUTO_NAO_ENCONTRADO(
            "001",
            "Produto nao encontrado",
            HttpStatus.NOT_FOUND
    ),

    PRODUTO_INDISPONIVEL(
            "002",
            "Produto indisponivel para venda",
            HttpStatus.BAD_REQUEST
    ),

    PRECO_INVALIDO(
            "003",
            "Preco deve ser maior que zero",
            HttpStatus.BAD_REQUEST
    ),

    DADOS_INVALIDOS(
            "004",
            "Dados invalidos",
            HttpStatus.BAD_REQUEST
    ),

    CATEGORIA_NAO_ENCONTRADA(
            "005",
            "Categoria nao encontrada",
            HttpStatus.NOT_FOUND
    ),

    CATEGORIA_INATIVA(
            "006",
            "Categoria inativa",
            HttpStatus.BAD_REQUEST
    ),

    CATEGORIA_NOME_DUPLICADO(
            "007",
            "Nome da categoria ja cadastrado",
            HttpStatus.CONFLICT
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
