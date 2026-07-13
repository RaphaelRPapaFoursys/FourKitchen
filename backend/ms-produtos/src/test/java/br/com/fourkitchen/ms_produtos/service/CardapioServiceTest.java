package br.com.fourkitchen.ms_produtos.service;

import br.com.fourkitchen.ms_produtos.dto.response.CategoriaCardapioResponse;
import br.com.fourkitchen.ms_produtos.dto.response.ProdutoCardapioResponse;
import br.com.fourkitchen.ms_produtos.mapper.CategoriaCardapioMapperSource;
import br.com.fourkitchen.ms_produtos.mapper.CategoriaCardapioResponseMapper;
import br.com.fourkitchen.ms_produtos.mapper.ProdutoCardapioResponseMapper;
import br.com.fourkitchen.ms_produtos.repository.ProdutoCardapioProjection;
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
        ProdutoCardapioProjection hamburguer = criarProduto(1, "Hamburguer", 1, "Lanches");
        ProdutoCardapioProjection batata = criarProduto(2, "Batata", 1, "Lanches");
        ProdutoCardapioProjection refrigerante = criarProduto(3, "Refrigerante", 2, "Bebidas");
        ProdutoCardapioResponse hamburguerResponse = criarProdutoResponse(hamburguer);
        ProdutoCardapioResponse batataResponse = criarProdutoResponse(batata);
        ProdutoCardapioResponse refrigeranteResponse = criarProdutoResponse(refrigerante);
        CategoriaCardapioResponse lanchesResponse = new CategoriaCardapioResponse(
                1,
                "Lanches",
                "Descricao Lanches",
                List.of(hamburguerResponse, batataResponse)
        );
        CategoriaCardapioResponse bebidasResponse = new CategoriaCardapioResponse(
                2,
                "Bebidas",
                "Descricao Bebidas",
                List.of(refrigeranteResponse)
        );

        when(produtoRepository.buscarProdutosDisponiveisParaCardapio())
                .thenReturn(List.of(hamburguer, batata, refrigerante));
        when(produtoCardapioResponseMapper.map(hamburguer)).thenReturn(hamburguerResponse);
        when(produtoCardapioResponseMapper.map(batata)).thenReturn(batataResponse);
        when(produtoCardapioResponseMapper.map(refrigerante)).thenReturn(refrigeranteResponse);
        when(categoriaCardapioResponseMapper.map(new CategoriaCardapioMapperSource(
                1,
                "Lanches",
                "Descricao Lanches",
                List.of(hamburguerResponse, batataResponse)
        ))).thenReturn(lanchesResponse);
        when(categoriaCardapioResponseMapper.map(new CategoriaCardapioMapperSource(
                2,
                "Bebidas",
                "Descricao Bebidas",
                List.of(refrigeranteResponse)
        ))).thenReturn(bebidasResponse);

        List<CategoriaCardapioResponse> resultado = cardapioService.buscarCardapio();

        assertEquals(List.of(lanchesResponse, bebidasResponse), resultado);
        verify(produtoRepository).buscarProdutosDisponiveisParaCardapio();
        verify(produtoCardapioResponseMapper).map(hamburguer);
        verify(produtoCardapioResponseMapper).map(batata);
        verify(produtoCardapioResponseMapper).map(refrigerante);
        verify(categoriaCardapioResponseMapper).map(new CategoriaCardapioMapperSource(
                1,
                "Lanches",
                "Descricao Lanches",
                List.of(hamburguerResponse, batataResponse)
        ));
        verify(categoriaCardapioResponseMapper).map(new CategoriaCardapioMapperSource(
                2,
                "Bebidas",
                "Descricao Bebidas",
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

    private ProdutoCardapioProjection criarProduto(Integer id, String nome, Integer categoriaId, String categoriaNome) {
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
                return "Descricao " + nome;
            }

            @Override
            public BigDecimal getPreco() {
                return new BigDecimal("19.90");
            }

            @Override
            public Integer getCategoriaId() {
                return categoriaId;
            }

            @Override
            public String getCategoriaNome() {
                return categoriaNome;
            }

            @Override
            public String getCategoriaDescricao() {
                return "Descricao " + categoriaNome;
            }

            @Override
            public Boolean getPossuiImagem() {
                return false;
            }
        };
    }

    private ProdutoCardapioResponse criarProdutoResponse(ProdutoCardapioProjection produto) {
        return new ProdutoCardapioResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                null,
                produto.getPreco()
        );
    }
}
