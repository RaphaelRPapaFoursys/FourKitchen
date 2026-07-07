package br.com.fourkitchen.ms_pedidos.exceptions;

public class PedidoNaoPermiteDecisaoException extends RuntimeException {
    public PedidoNaoPermiteDecisaoException() {
        super("O pedido deve estar com status AGUARDANDO_DECISAO para receber uma decisão.");
    }
}
