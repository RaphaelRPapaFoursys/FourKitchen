package br.com.fourkitchen.ms_produtos.service;

import br.com.fourkitchen.ms_produtos.dto.response.CategoriaCardapioResponse;
import br.com.fourkitchen.ms_produtos.dto.response.ProdutoCardapioResponse;
import br.com.fourkitchen.ms_produtos.mapper.CategoriaCardapioMapperSource;
import br.com.fourkitchen.ms_produtos.mapper.CategoriaCardapioResponseMapper;
import br.com.fourkitchen.ms_produtos.mapper.ProdutoCardapioResponseMapper;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import br.com.fourkitchen.ms_produtos.model.Produto;
import br.com.fourkitchen.ms_produtos.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardapioServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ProdutoCardapioResponseMapper produtoCardapioResponseMapper;

    @Mock
    private CategoriaCardapioResponseMapper categoriaCardapioResponseMapper;

    @InjectMocks
    private CardapioService cardapioService;

    @Test
    void buscarCardapioDeveRetornarProdutosDisponiveisAgrupadosPorCategoria() {
        Categoria lanches = criarCategoria(1, "Lanches");
        Categoria bebidas = criarCategoria(2, "Bebidas");
        Produto hamburguer = criarProduto(1, "Hamburguer", lanches, true);
        Produto batata = criarProduto(2, "Batata", lanches, true);
        Produto refrigerante = criarProduto(3, "Refrigerante", bebidas, true);
        ProdutoCardapioResponse hamburguerResponse = criarProdutoResponse(hamburguer);
        ProdutoCardapioResponse batataResponse = criarProdutoResponse(batata);
        ProdutoCardapioResponse refrigeranteResponse = criarProdutoResponse(refrigerante);
        CategoriaCardapioResponse lanchesResponse = new CategoriaCardapioResponse(
                1,
                "Lanches",
                "Descricao Lanches",
                "imagem-lanches",
                List.of(hamburguerResponse, batataResponse)
        );
        CategoriaCardapioResponse bebidasResponse = new CategoriaCardapioResponse(
                2,
                "Bebidas",
                "Descricao Bebidas",
                "imagem-bebidas",
                List.of(refrigeranteResponse)
        );

        when(produtoRepository.buscarProdutosDisponiveisParaCardapio())
                .thenReturn(List.of(hamburguer, batata, refrigerante));
        when(produtoCardapioResponseMapper.map(hamburguer)).thenReturn(hamburguerResponse);
        when(produtoCardapioResponseMapper.map(batata)).thenReturn(batataResponse);
        when(produtoCardapioResponseMapper.map(refrigerante)).thenReturn(refrigeranteResponse);
        when(categoriaCardapioResponseMapper.map(new CategoriaCardapioMapperSource(
                lanches,
                List.of(hamburguerResponse, batataResponse)
        ))).thenReturn(lanchesResponse);
        when(categoriaCardapioResponseMapper.map(new CategoriaCardapioMapperSource(
                bebidas,
                List.of(refrigeranteResponse)
        ))).thenReturn(bebidasResponse);

        List<CategoriaCardapioResponse> resultado = cardapioService.buscarCardapio();

        assertEquals(List.of(lanchesResponse, bebidasResponse), resultado);
        verify(produtoRepository).buscarProdutosDisponiveisParaCardapio();
        verify(produtoCardapioResponseMapper).map(hamburguer);
        verify(produtoCardapioResponseMapper).map(batata);
        verify(produtoCardapioResponseMapper).map(refrigerante);
        verify(categoriaCardapioResponseMapper).map(new CategoriaCardapioMapperSource(
                lanches,
                List.of(hamburguerResponse, batataResponse)
        ));
        verify(categoriaCardapioResponseMapper).map(new CategoriaCardapioMapperSource(
                bebidas,
                List.of(refrigeranteResponse)
        ));
    }

    @Test
    void buscarCardapioDeveRetornarListaVaziaQuandoNaoHouverProdutosDisponiveis() {
        when(produtoRepository.buscarProdutosDisponiveisParaCardapio()).thenReturn(List.of());

        List<CategoriaCardapioResponse> resultado = cardapioService.buscarCardapio();

        assertEquals(List.of(), resultado);
        verify(produtoRepository).buscarProdutosDisponiveisParaCardapio();
        verifyNoInteractions(produtoCardapioResponseMapper, categoriaCardapioResponseMapper);
    }

    private Categoria criarCategoria(Integer id, String nome) {
        return Categoria.builder()
                .id(id)
                .nome(nome)
                .descricao("Descricao " + nome)
                .ativo(true)
                .build();
    }

    private Produto criarProduto(Integer id, String nome, Categoria categoria, Boolean disponivel) {
        return Produto.builder()
                .id(id)
                .nome(nome)
                .descricao("Descricao " + nome)
                .preco(new BigDecimal("19.90"))
                .categoria(categoria)
                .disponivel(disponivel)
                .build();
    }

    private ProdutoCardapioResponse criarProdutoResponse(Produto produto) {
        return new ProdutoCardapioResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                null,
                produto.getPreco()
        );
    }
}
