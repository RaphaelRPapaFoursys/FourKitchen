package br.com.fourkitchen.ms_pedidos.mapper;

import br.com.fourkitchen.ms_pedidos.dto.request.CriarProdutoPedidoRequest;
import br.com.fourkitchen.ms_pedidos.entities.ProdutoPedido;
import org.springframework.stereotype.Component;

@Component
public class CriarProdutoPedidoRequestMapper implements Mapper<CriarProdutoPedidoRequest, ProdutoPedido> {
    @Override
    public ProdutoPedido map(CriarProdutoPedidoRequest produtoPedidoRequest) {
        ProdutoPedido produtoPedido = ProdutoPedido
                .builder()
                .quantidade(produtoPedidoRequest.quantidade())
                .idPedido(produtoPedidoRequest.idPedido())
                .idProduto(produtoPedidoRequest.idProduto())
                .precoUnitario(produtoPedidoRequest.precoUnitario())
                .observacao(produtoPedidoRequest.observacao())
                .build();

        return produtoPedido;
    }
}
