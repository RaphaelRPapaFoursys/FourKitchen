package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ProdutoPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.ItemPedidoTotemRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemPedidoTotemRequestMapperTest {

    private final ItemPedidoTotemRequestMapper mapper = new ItemPedidoTotemRequestMapper();

    @Test
    void mapDeveMapearItemComPrecoAtualDoProduto() {
        ItemPedidoTotemRequest item = new ItemPedidoTotemRequest(10, 2, "Sem cebola");
        ProdutoDisponibilidadeResponse disponibilidade = new ProdutoDisponibilidadeResponse(
                10,
                true,
                new BigDecimal("29.90")
        );

        ProdutoPedidoRequest response = mapper.map(new ItemPedidoTotemMapperSource(item, disponibilidade));

        assertEquals(10, response.idProduto());
        assertEquals(2, response.quantidade());
        assertEquals(new BigDecimal("29.90"), response.precoUnitario());
        assertEquals("Sem cebola", response.observacao());
    }
}
