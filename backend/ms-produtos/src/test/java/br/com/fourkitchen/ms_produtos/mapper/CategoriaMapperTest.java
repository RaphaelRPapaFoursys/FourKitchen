package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.request.AtualizarCategoriaRequest;
import br.com.fourkitchen.ms_produtos.dto.request.CriarCategoriaRequest;
import br.com.fourkitchen.ms_produtos.dto.response.CategoriaResponse;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import org.junit.jupiter.api.Test;

import static br.com.fourkitchen.ms_produtos.support.ImagemTesteFactory.criarPng;
import static br.com.fourkitchen.ms_produtos.support.ImagemTesteFactory.paraBase64;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoriaMapperTest {

    private final ImagemBase64Mapper imagemBase64Mapper = new ImagemBase64Mapper();

    private final CriarCategoriaRequestMapper criarCategoriaRequestMapper =
            new CriarCategoriaRequestMapper(imagemBase64Mapper);

    private final AtualizarCategoriaRequestMapper atualizarCategoriaRequestMapper =
            new AtualizarCategoriaRequestMapper(imagemBase64Mapper);

    private final CategoriaResponseMapper categoriaResponseMapper =
            new CategoriaResponseMapper(imagemBase64Mapper);

    @Test
    void criarCategoriaRequestMapperDeveMapearImagemBase64() {
        byte[] imagem = criarPng(800, 600);
        CriarCategoriaRequest request = new CriarCategoriaRequest(
                " Lanches ",
                "Sanduiches",
                paraBase64(imagem)
        );

        Categoria categoria = criarCategoriaRequestMapper.map(request);

        assertEquals("Lanches", categoria.getNome());
        assertEquals("Sanduiches", categoria.getDescricao());
        assertArrayEquals(imagem, categoria.getImagem());
    }

    @Test
    void atualizarCategoriaRequestMapperDevePreservarImagemQuandoImagemNaoForEnviada() {
        byte[] imagem = criarPng(800, 600);
        Categoria categoria = Categoria.builder()
                .nome("Lanches")
                .imagem(imagem)
                .build();
        AtualizarCategoriaRequest request = new AtualizarCategoriaRequest(
                "Sanduiches",
                "Artesanais",
                null
        );

        atualizarCategoriaRequestMapper.map(request, categoria);

        assertEquals("Sanduiches", categoria.getNome());
        assertEquals("Artesanais", categoria.getDescricao());
        assertArrayEquals(imagem, categoria.getImagem());
    }

    @Test
    void categoriaResponseMapperDeveConverterImagemParaBase64() {
        byte[] imagem = criarPng(800, 600);
        Categoria categoria = Categoria.builder()
                .id(1)
                .nome("Lanches")
                .descricao("Sanduiches")
                .imagem(imagem)
                .ativo(true)
                .build();

        CategoriaResponse response = categoriaResponseMapper.map(categoria);

        assertEquals(1, response.id());
        assertEquals("Lanches", response.nome());
        assertEquals("Sanduiches", response.descricao());
        assertEquals(paraBase64(imagem), response.imagem());
        assertEquals(true, response.ativo());
    }
}
