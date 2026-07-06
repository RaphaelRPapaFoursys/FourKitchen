package br.com.fourkitchen.ms_pedidos.exceptions;

public class PedidoAguardandoDecisaoException extends RuntimeException {
    public PedidoAguardandoDecisaoException () {
        super("Pedido aguardando decisão.");
    }
}
