package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.ItemPedidoTotemRequest;

public record ItemPedidoTotemMapperSource(
        ItemPedidoTotemRequest item,
        ProdutoDisponibilidadeResponse disponibilidade
) {
}
