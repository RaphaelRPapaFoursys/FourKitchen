package br.com.fourkitchen.ms_pedidos.enums;

import lombok.Getter;

@Getter
public enum StatusPedido {
    ENVIADO_COZINHA,
    EM_PREPARO,
    PRONTO,
    ENTREGUE,
    FINALIZADO,
    CANCELADO,
    AGUARDANDO_DECISAO;
}