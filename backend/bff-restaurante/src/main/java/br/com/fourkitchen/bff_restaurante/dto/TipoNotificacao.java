package br.com.fourkitchen.bff_restaurante.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Tipo da notificacao. A mensagem exibida e gerada automaticamente pelo ms-notificacoes.")
public enum TipoNotificacao {

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
