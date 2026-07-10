package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.request.AtualizarProdutoRequest;
import br.com.fourkitchen.ms_produtos.dto.request.CriarProdutoRequest;
import br.com.fourkitchen.ms_produtos.model.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static br.com.fourkitchen.ms_produtos.support.ImagemTesteFactory.criarPng;
import static br.com.fourkitchen.ms_produtos.support.ImagemTesteFactory.paraBase64;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        assertEquals(new BigDecimal("29.90"), produto.getPreco());
    }

    @Test
    void atualizarProdutoRequestMapperDevePreservarImagemAtualQuandoImagemNaoForEnviada() {
        Produto produto = Produto.builder()
                .imagem("imagem antiga".getBytes())
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
    }
}
