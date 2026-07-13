package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaCardapioClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoCardapioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaCardapioResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardapioResponseMapperTest {

    private final CardapioResponseMapper cardapioResponseMapper = new CardapioResponseMapper();

    @Test
    void mapDeveMapearCategoriaEProdutosDoClientParaRespostaDoBff() {
        CategoriaCardapioClientResponse source = new CategoriaCardapioClientResponse(
                1,
                "Lanches",
                "Sanduiches",
                "imagem-categoria",
                List.of(new ProdutoCardapioClientResponse(
                        10,
                        "X-Burger",
                        "Pao, carne e queijo",
                        "aW1hZ2Vt",
                        new BigDecimal("29.90")
                ))
        );

        CategoriaCardapioResponse response = cardapioResponseMapper.map(source);

        assertEquals(1, response.categoriaId());
        assertEquals("Lanches", response.categoriaNome());
        assertEquals("Sanduiches", response.categoriaDescricao());
        assertEquals("imagem-categoria", response.categoriaImagem());
        assertEquals(1, response.produtos().size());
        assertEquals(10, response.produtos().getFirst().id());
        assertEquals("X-Burger", response.produtos().getFirst().nome());
        assertEquals("Pao, carne e queijo", response.produtos().getFirst().descricao());
        assertEquals("aW1hZ2Vt", response.produtos().getFirst().imagem());
        assertEquals(new BigDecimal("29.90"), response.produtos().getFirst().preco());
    }

    @Test
    void mapDeveRetornarListaVaziaQuandoProdutosVierNulo() {
        CategoriaCardapioClientResponse source = new CategoriaCardapioClientResponse(
                1,
                "Lanches",
                "Sanduiches",
                null,
                null
        );

        CategoriaCardapioResponse response = cardapioResponseMapper.map(source);

        assertEquals(List.of(), response.produtos());
    }
}
