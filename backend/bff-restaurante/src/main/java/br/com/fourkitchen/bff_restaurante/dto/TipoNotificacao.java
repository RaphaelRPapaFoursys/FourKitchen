package br.com.fourkitchen.bff_restaurante.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Tipo da notificacao. A mensagem exibida e gerada automaticamente pelo ms-notificacoes.")
public enum TipoNotificacao {

    PEDIDO_EM_PREPARO("Pedido em preparo"),
    PEDIDO_PRONTO("Pedido pronto para retirada"),
    PEDIDO_INDISPONIVEL("Pedido com item indisponivel"),
    PEDIDO_COM_PROBLEMA("Pedido com item com problema."),
    PEDIDO_ERRO("Pedido com erro"),
    PEDIDO_COM_FALTA("Pedido com item em falta"),
    PEDIDO_CANCELADO("Pedido cancelado"),
    CHAMADA_GARCOM("Cliente solicitou atendimento"),
    CONTA_SOLICITADA("Cliente solicitou fechamento da conta"),
    ALTERACAO_PEDIDO_SOLICITADA("Cliente solicitou alteracao no pedido");

    private final String mensagemPadrao;

    TipoNotificacao(String mensagemPadrao) {
        this.mensagemPadrao = mensagemPadrao;
    }
}
