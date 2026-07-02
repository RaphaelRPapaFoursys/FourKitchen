package br.com.fourkitchen.ms_notificacoes.enums;

import lombok.Getter;

@Getter
public enum TipoNotificacao {

    PEDIDO_EM_PREPARO("Pedido em preparo"),
    PEDIDO_PRONTO("Pedido pronto para retirada"),
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
