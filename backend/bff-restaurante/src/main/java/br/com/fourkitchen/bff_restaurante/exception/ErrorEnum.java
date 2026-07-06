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

    SESSAO_MESA_INVALIDA(
            "006",
            "Sessao da mesa invalida",
            HttpStatus.BAD_REQUEST
    ),

    MS_MESAS_INDISPONIVEL(
            "007",
            "Servico de mesas indisponivel",
            HttpStatus.BAD_GATEWAY
    ),

    MS_PEDIDOS_INDISPONIVEL(
            "008",
            "Servico de pedidos indisponivel",
            HttpStatus.BAD_GATEWAY
    ),

    NOTIFICACAO_NAO_ENCONTRADA(
            "009",
            "Notificacao nao encontrada",
            HttpStatus.NOT_FOUND
    ),

    MS_NOTIFICACOES_INDISPONIVEL(
            "010",
            "Servico de notificacoes indisponivel",
            HttpStatus.BAD_GATEWAY
    ),

    PRODUTO_INDISPONIVEL(
            "011",
            "Produto indisponivel",
            HttpStatus.BAD_REQUEST
    ),

    MS_PRODUTOS_INDISPONIVEL(
            "012",
            "Servico de produtos indisponivel",
            HttpStatus.BAD_GATEWAY
    ),

    MESA_NAO_ATRIBUIDA_AO_GARCOM(
            "013",
            "Mesa nao atribuida ao garcom",
            HttpStatus.FORBIDDEN
    ),

    PEDIDO_NAO_ENCONTRADO(
            "014",
            "Pedido nao encontrado",
            HttpStatus.NOT_FOUND
    ),

    TRANSICAO_STATUS_INVALIDA(
            "015",
            "Transicao de status invalida",
            HttpStatus.BAD_REQUEST
    ),

    CHAMADA_GARCOM_INVALIDA(
            "016",
            "Chamada de garcom invalida",
            HttpStatus.BAD_REQUEST
    ),

    CHAMADA_GARCOM_NAO_PERTENCE_AO_GARCOM(
            "017",
            "Chamada de garcom nao pertence ao garcom",
            HttpStatus.FORBIDDEN
    ),

    MESA_SEM_GARCOM(
            "018",
            "Mesa sem garcom responsavel",
            HttpStatus.BAD_REQUEST
    ),

    GARCOM_INVALIDO(
            "019",
            "Garcom invalido",
            HttpStatus.BAD_REQUEST
    ),

    MESA_NAO_ENCONTRADA(
            "020",
            "Mesa nao encontrada",
            HttpStatus.NOT_FOUND
    ),

    ATENDIMENTO_NAO_ABERTO(
            "019",
            "Mesa sem atendimento aberto",
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
