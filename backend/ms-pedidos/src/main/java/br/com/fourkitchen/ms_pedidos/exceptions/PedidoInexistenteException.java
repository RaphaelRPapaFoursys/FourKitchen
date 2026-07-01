package br.com.fourkitchen.ms_pedidos.exceptions;

public class PedidoInexistenteException extends RuntimeException {
    public PedidoInexistenteException() {
        super("Pedido inexistente.");
    }
}
