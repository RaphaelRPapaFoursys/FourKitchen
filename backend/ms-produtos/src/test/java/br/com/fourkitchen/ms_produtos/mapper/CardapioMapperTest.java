package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.CategoriaCardapioResponse;
import br.com.fourkitchen.ms_produtos.dto.response.ProdutoCardapioResponse;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import br.com.fourkitchen.ms_produtos.model.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CardapioMapperTest {

    private final ImagemBase64Mapper imagemBase64Mapper = new ImagemBase64Mapper();

    private final ProdutoCardapioResponseMapper produtoCardapioResponseMapper =
            new ProdutoCardapioResponseMapper(imagemBase64Mapper);

    private final CategoriaCardapioResponseMapper categoriaCardapioResponseMapper = new CategoriaCardapioResponseMapper();

    @Test
    void produtoCardapioResponseMapperDeveMapearProduto() {
        Produto produto = Produto.builder()
                .id(1)
                .nome("Hamburguer")
                .descricao("Artesanal")
                .imagem("imagem".getBytes())
                .preco(new BigDecimal("29.90"))
                .build();

        ProdutoCardapioResponse response = produtoCardapioResponseMapper.map(produto);

        assertEquals(1, response.id());
        assertEquals("Hamburguer", response.nome());
        assertEquals("Artesanal", response.descricao());
        assertEquals("aW1hZ2Vt", response.imagem());
        assertEquals(new BigDecimal("29.90"), response.preco());
    }

    @Test
    void categoriaCardapioResponseMapperDeveMapearCategoriaComProdutos() {
        Categoria categoria = Categoria.builder()
                .id(1)
                .nome("Lanches")
                .descricao("Sanduiches")
                .build();
        List<ProdutoCardapioResponse> produtos = List.of(
                new ProdutoCardapioResponse(1, "Hamburguer", "Artesanal", null, new BigDecimal("29.90"))
        );

        CategoriaCardapioResponse response = categoriaCardapioResponseMapper.map(
                new CategoriaCardapioMapperSource(categoria, produtos)
        );

        assertEquals(1, response.categoriaId());
        assertEquals("Lanches", response.categoriaNome());
        assertEquals("Sanduiches", response.categoriaDescricao());
        assertSame(produtos, response.produtos());
    }
}
