package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.CategoriaCardapioResponse;
import br.com.fourkitchen.ms_produtos.dto.response.ProdutoCardapioResponse;
import br.com.fourkitchen.ms_produtos.repository.ProdutoCardapioProjection;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CardapioMapperTest {

    private final ProdutoCardapioResponseMapper produtoCardapioResponseMapper =
            new ProdutoCardapioResponseMapper();

    private final CategoriaCardapioResponseMapper categoriaCardapioResponseMapper = new CategoriaCardapioResponseMapper();

    @Test
    void produtoCardapioResponseMapperDeveMapearProduto() {
        Instant imagemAtualizadaEm = Instant.parse("2026-07-13T15:30:00Z");
        ProdutoCardapioProjection produto = criarProdutoProjection(
                1,
                "Hamburguer",
                "Artesanal",
                true,
                imagemAtualizadaEm
        );

        ProdutoCardapioResponse response = produtoCardapioResponseMapper.map(produto);

        assertEquals(1, response.id());
        assertEquals("Hamburguer", response.nome());
        assertEquals("Artesanal", response.descricao());
        assertEquals("/api/produtos/1/imagem?v=1783956600000", response.imagemUrl());
        assertEquals(new BigDecimal("29.90"), response.preco());
    }

    @Test
    void categoriaCardapioResponseMapperDeveMapearCategoriaComProdutos() {
        List<ProdutoCardapioResponse> produtos = List.of(
                new ProdutoCardapioResponse(1, "Hamburguer", "Artesanal", null, new BigDecimal("29.90"))
        );

        CategoriaCardapioResponse response = categoriaCardapioResponseMapper.map(
                new CategoriaCardapioMapperSource(1, "Lanches", "Sanduiches", produtos)
        );

        assertEquals(1, response.categoriaId());
        assertEquals("Lanches", response.categoriaNome());
        assertEquals("Sanduiches", response.categoriaDescricao());
        assertSame(produtos, response.produtos());
    }

    private ProdutoCardapioProjection criarProdutoProjection(
            Integer id,
            String nome,
            String descricao,
            Boolean possuiImagem,
            Instant imagemAtualizadaEm
    ) {
        return new ProdutoCardapioProjection() {
            @Override
            public Integer getId() {
                return id;
            }

            @Override
            public String getNome() {
                return nome;
            }

            @Override
            public String getDescricao() {
                return descricao;
            }

            @Override
            public BigDecimal getPreco() {
                return new BigDecimal("29.90");
            }

            @Override
            public Integer getCategoriaId() {
                return 1;
            }

            @Override
            public String getCategoriaNome() {
                return "Lanches";
            }

            @Override
            public String getCategoriaDescricao() {
                return "Sanduiches";
            }

            @Override
            public Boolean getPossuiImagem() {
                return possuiImagem;
            }

            @Override
            public Instant getImagemAtualizadaEm() {
                return imagemAtualizadaEm;
            }
        };
    }
}
