package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.ms_produtos.model.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProdutoDisponibilidadeResponseMapperTest {

    private final ProdutoDisponibilidadeResponseMapper mapper = new ProdutoDisponibilidadeResponseMapper();

    @Test
    void mapDeveRetornarDisponibilidadeEPrecoAtual() {
        Produto produto = Produto.builder()
                .id(10)
                .nome("X-Burger")
                .preco(new BigDecimal("29.90"))
                .disponivel(true)
                .build();

        ProdutoDisponibilidadeResponse response = mapper.map(produto);

        assertEquals(10, response.produtoId());
        assertEquals("X-Burger", response.nome());
        assertEquals(true, response.disponivel());
        assertEquals(new BigDecimal("29.90"), response.preco());
    }
}
