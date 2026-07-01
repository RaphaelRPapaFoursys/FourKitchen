package br.com.fourkitchen.ms_pedidos.exceptions;

public class ProdutoPedidoInexistenteException extends RuntimeException {
    public ProdutoPedidoInexistenteException() {
        super("ProdutoPedido inexistente.");
    }
}
