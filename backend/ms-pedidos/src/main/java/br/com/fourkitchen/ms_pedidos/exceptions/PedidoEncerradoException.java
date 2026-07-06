package br.com.fourkitchen.ms_pedidos.exceptions;

public class PedidoEncerradoException extends RuntimeException {
    public PedidoEncerradoException() {
        super("Pedido encerrado.");
    }
}
