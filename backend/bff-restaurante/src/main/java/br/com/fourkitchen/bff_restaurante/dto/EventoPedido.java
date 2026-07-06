package br.com.fourkitchen.bff_restaurante.dto;

public enum EventoPedido {
    PEDIDO_EM_PREPARO(
            TipoNotificacao.PEDIDO_EM_PREPARO,
            DestinoNotificacao.COZINHA
    ),
    PEDIDO_PRONTO(
            TipoNotificacao.PEDIDO_PRONTO,
            DestinoNotificacao.GARCOM
    ),

    PEDIDO_COM_FALTA(
            TipoNotificacao.PEDIDO_COM_FALTA,
            DestinoNotificacao.GARCOM
    ),
    PEDIDO_ERRO(
         TipoNotificacao.PEDIDO_ERRO,
         DestinoNotificacao.GARCOM
    ),
    PEDIDO_INDISPONIVEL(
            TipoNotificacao.PEDIDO_INDISPONIVEL,
            DestinoNotificacao.GARCOM
    );


    private final TipoNotificacao tipoNotificacao;

    private final DestinoNotificacao destino;

    EventoPedido(TipoNotificacao tipoNotificacao, DestinoNotificacao destino) {
        this.tipoNotificacao = tipoNotificacao;
        this.destino = destino;
    }

    public TipoNotificacao tipoNotificacao() {
        return tipoNotificacao;
    }

    public DestinoNotificacao destino() {
        return destino;
    }
}
