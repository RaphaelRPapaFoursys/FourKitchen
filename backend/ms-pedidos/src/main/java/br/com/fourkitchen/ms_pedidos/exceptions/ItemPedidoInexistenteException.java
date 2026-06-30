package br.com.fourkitchen.ms_pedidos.exceptions;

public class ItemPedidoInexistenteException extends RuntimeException {
    public ItemPedidoInexistenteException() {
        super("Item Pedido inexistente.");
    }
}
