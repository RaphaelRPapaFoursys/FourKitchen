package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.request.AtualizarProdutoRequest;
import br.com.fourkitchen.ms_produtos.dto.request.CriarProdutoRequest;
import br.com.fourkitchen.ms_produtos.model.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static br.com.fourkitchen.ms_produtos.support.ImagemTesteFactory.criarPng;
import static br.com.fourkitchen.ms_produtos.support.ImagemTesteFactory.paraBase64;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProdutoMapperTest {

    private final ImagemBase64Mapper imagemBase64Mapper = new ImagemBase64Mapper();

    private final CriarProdutoRequestMapper criarProdutoRequestMapper =
            new CriarProdutoRequestMapper(imagemBase64Mapper);

    private final AtualizarProdutoRequestMapper atualizarProdutoRequestMapper =
            new AtualizarProdutoRequestMapper(imagemBase64Mapper);

    @Test
    void criarProdutoRequestMapperDeveMapearImagemBase64() {
        byte[] imagem = criarPng(800, 600);
        CriarProdutoRequest request = new CriarProdutoRequest(
                "Hamburguer",
                "Artesanal",
                paraBase64(imagem),
                new BigDecimal("29.90"),
                1
        );

        Produto produto = criarProdutoRequestMapper.map(request);

        assertEquals("Hamburguer", produto.getNome());
        assertEquals("Artesanal", produto.getDescricao());
        assertArrayEquals(imagem, produto.getImagem());
        assertNotNull(produto.getImagemAtualizadaEm());
        assertEquals(new BigDecimal("29.90"), produto.getPreco());
    }

    @Test
    void atualizarProdutoRequestMapperDevePreservarImagemAtualQuandoImagemNaoForEnviada() {
        Instant versaoAtual = Instant.parse("2026-07-13T15:30:00Z");
        Produto produto = Produto.builder()
                .imagem("imagem antiga".getBytes())
                .imagemAtualizadaEm(versaoAtual)
                .build();
        AtualizarProdutoRequest request = new AtualizarProdutoRequest(
                "Hamburguer",
                "Artesanal",
                null,
                new BigDecimal("29.90"),
                1
        );

        atualizarProdutoRequestMapper.map(request, produto);

        assertArrayEquals("imagem antiga".getBytes(), produto.getImagem());
        assertEquals(versaoAtual, produto.getImagemAtualizadaEm());
    }

    @Test
    void atualizarProdutoRequestMapperDeveRemoverImagemQuandoImagemForVazia() {
        Produto produto = Produto.builder()
                .imagem("imagem antiga".getBytes())
                .build();
        AtualizarProdutoRequest request = new AtualizarProdutoRequest(
                "Hamburguer",
                "Artesanal",
                "",
                new BigDecimal("29.90"),
                1
        );

        atualizarProdutoRequestMapper.map(request, produto);

        assertNull(produto.getImagem());
        assertNotNull(produto.getImagemAtualizadaEm());
    }
}
