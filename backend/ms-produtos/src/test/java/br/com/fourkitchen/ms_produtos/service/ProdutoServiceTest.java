package br.com.fourkitchen.ms_produtos.service;

import br.com.fourkitchen.ms_produtos.dto.request.AtualizarProdutoRequest;
import br.com.fourkitchen.ms_produtos.dto.request.CriarProdutoRequest;
import br.com.fourkitchen.ms_produtos.dto.response.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.ms_produtos.dto.response.ProdutoResponse;
import br.com.fourkitchen.ms_produtos.exception.BaseException;
import br.com.fourkitchen.ms_produtos.exception.ErrorEnum;
import br.com.fourkitchen.ms_produtos.mapper.AtualizarProdutoRequestMapper;
import br.com.fourkitchen.ms_produtos.mapper.CriarProdutoRequestMapper;
import br.com.fourkitchen.ms_produtos.mapper.ProdutoDisponibilidadeResponseMapper;
import br.com.fourkitchen.ms_produtos.mapper.ProdutoResponseMapper;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import br.com.fourkitchen.ms_produtos.model.Produto;
import br.com.fourkitchen.ms_produtos.repository.CategoriaRepository;
import br.com.fourkitchen.ms_produtos.repository.ProdutoRepository;
import br.com.fourkitchen.ms_produtos.repository.ProdutoGestorProjection;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProdutoResponseMapper produtoResponseMapper;

    @Mock
    private ProdutoDisponibilidadeResponseMapper produtoDisponibilidadeResponseMapper;

    @Mock
    private CriarProdutoRequestMapper criarProdutoRequestMapper;

    @Mock
    private AtualizarProdutoRequestMapper atualizarProdutoRequestMapper;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    void listarProdutosDeveRetornarProdutosMapeados() {
        ProdutoGestorProjection produto = mock(ProdutoGestorProjection.class);
        ProdutoResponse response = new ProdutoResponse(1, "Hamburguer", "Artesanal", null,
                new BigDecimal("29.90"), 1, "Lanches", true);
        PageRequest pageable = PageRequest.of(0, 10);

        when(produtoRepository.buscarProdutosParaGestao(pageable))
                .thenReturn(new PageImpl<>(List.of(produto), pageable, 1));
        when(produtoResponseMapper.map(produto)).thenReturn(response);

        var resultado = produtoService.listarProdutos(null, pageable);

        assertEquals(List.of(response), resultado.content());
        assertEquals(1, resultado.totalElements());
        verify(produtoRepository).buscarProdutosParaGestao(pageable);
        verify(produtoResponseMapper).map(produto);
    }

    @Test
    void listarProdutosDisponiveisDeveRetornarSomenteProdutosDisponiveisMapeados() {
        Categoria categoria = criarCategoria(1, "Lanches", true);
        Produto produto = criarProduto(1, "Hamburguer", "Artesanal", "29.90", categoria, true);
        ProdutoResponse response = criarResponse(produto);

        when(produtoRepository.findByDisponivelTrue()).thenReturn(List.of(produto));
        when(produtoResponseMapper.map(produto)).thenReturn(response);

        List<ProdutoResponse> resultado = produtoService.listarProdutosDisponiveis();

        assertEquals(List.of(response), resultado);
        verify(produtoRepository).findByDisponivelTrue();
        verify(produtoResponseMapper).map(produto);
    }

    @Test
    void criarProdutoDeveSalvarProdutoDisponivelComCategoriaAtiva() {
        CriarProdutoRequest request = new CriarProdutoRequest(
                "Hamburguer",
                "Artesanal",
                null,
                new BigDecimal("29.90"),
                1
        );
        Categoria categoria = criarCategoria(1, "Lanches", true);
        Produto produtoMapeado = criarProduto(null, "Hamburguer", "Artesanal", "29.90", null, null);
        Produto produtoSalvo = criarProduto(1, "Hamburguer", "Artesanal", "29.90", categoria, true);
        ProdutoResponse response = criarResponse(produtoSalvo);

        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(criarProdutoRequestMapper.map(request)).thenReturn(produtoMapeado);
        when(produtoRepository.save(produtoMapeado)).thenReturn(produtoSalvo);
        when(produtoResponseMapper.map(produtoSalvo)).thenReturn(response);

        ProdutoResponse resultado = produtoService.criarProduto(request);

        assertSame(response, resultado);

        ArgumentCaptor<Produto> produtoCaptor = ArgumentCaptor.forClass(Produto.class);
        verify(produtoRepository).save(produtoCaptor.capture());

        Produto produtoEnviadoParaSalvar = produtoCaptor.getValue();
        assertEquals(true, produtoEnviadoParaSalvar.getDisponivel());
        assertSame(categoria, produtoEnviadoParaSalvar.getCategoria());
        verify(categoriaRepository).findById(1);
        verify(criarProdutoRequestMapper).map(request);
        verify(produtoResponseMapper).map(produtoSalvo);
    }

    @Test
    void criarProdutoDeveLancarExcecaoQuandoPrecoForInvalido() {
        CriarProdutoRequest request = new CriarProdutoRequest(
                "Hamburguer",
                "Artesanal",
                null,
                BigDecimal.ZERO,
                1
        );

        BaseException exception = assertThrows(BaseException.class, () -> produtoService.criarProduto(request));

        assertEquals(ErrorEnum.PRECO_INVALIDO, exception.getErrorEnum());

        verify(produtoRepository, never()).save(any());
        verifyNoInteractions(categoriaRepository, criarProdutoRequestMapper, produtoResponseMapper);
    }

    @Test
    void criarProdutoDeveLancarExcecaoQuandoCategoriaNaoExistir() {
        CriarProdutoRequest request = new CriarProdutoRequest(
                "Hamburguer",
                "Artesanal",
                null,
                new BigDecimal("29.90"),
                99
        );

        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        BaseException exception = assertThrows(BaseException.class, () -> produtoService.criarProduto(request));

        assertEquals(ErrorEnum.CATEGORIA_NAO_ENCONTRADA, exception.getErrorEnum());
        verify(categoriaRepository).findById(99);
        verify(produtoRepository, never()).save(any());
        verifyNoInteractions(criarProdutoRequestMapper, produtoResponseMapper);
    }

    @Test
    void criarProdutoDeveLancarExcecaoQuandoCategoriaEstiverInativa() {
        CriarProdutoRequest request = new CriarProdutoRequest(
                "Hamburguer",
                "Artesanal",
                null,
                new BigDecimal("29.90"),
                1
        );
        Categoria categoria = criarCategoria(1, "Lanches", false);

        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));

        BaseException exception = assertThrows(BaseException.class, () -> produtoService.criarProduto(request));

        assertEquals(ErrorEnum.CATEGORIA_INATIVA, exception.getErrorEnum());
        verify(categoriaRepository).findById(1);
        verify(produtoRepository, never()).save(any());
        verifyNoInteractions(criarProdutoRequestMapper, produtoResponseMapper);
    }

    @Test
    void atualizarProdutoDeveAlterarDadosDoProdutoECategoria() {
        AtualizarProdutoRequest request = new AtualizarProdutoRequest(
                "Hamburguer Duplo",
                "Artesanal duplo",
                null,
                new BigDecimal("39.90"),
                2
        );
        Categoria categoriaAntiga = criarCategoria(1, "Lanches", true);
        Categoria categoriaNova = criarCategoria(2, "Promocoes", true);
        Produto produto = criarProduto(1, "Hamburguer", "Artesanal", "29.90", categoriaAntiga, true);
        Produto produtoSalvo = criarProduto(1, "Hamburguer Duplo", "Artesanal duplo", "39.90", categoriaNova, true);
        ProdutoResponse response = criarResponse(produtoSalvo);

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(categoriaRepository.findById(2)).thenReturn(Optional.of(categoriaNova));
        when(produtoRepository.save(produto)).thenReturn(produtoSalvo);
        when(produtoResponseMapper.map(produtoSalvo)).thenReturn(response);

        ProdutoResponse resultado = produtoService.atualizarProduto(1, request);

        assertSame(response, resultado);
        assertSame(categoriaNova, produto.getCategoria());
        verify(produtoRepository).findById(1);
        verify(categoriaRepository).findById(2);
        verify(atualizarProdutoRequestMapper).map(request, produto);
        verify(produtoRepository).save(produto);
        verify(produtoResponseMapper).map(produtoSalvo);
    }

    @Test
    void desativarProdutoDeveSalvarProdutoIndisponivel() {
        Categoria categoria = criarCategoria(1, "Lanches", true);
        Produto produto = criarProduto(1, "Hamburguer", "Artesanal", "29.90", categoria, true);
        Produto produtoSalvo = criarProduto(1, "Hamburguer", "Artesanal", "29.90", categoria, false);
        ProdutoResponse response = criarResponse(produtoSalvo);

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(produto)).thenReturn(produtoSalvo);
        when(produtoResponseMapper.map(produtoSalvo)).thenReturn(response);

        ProdutoResponse resultado = produtoService.desativarProduto(1);

        assertSame(response, resultado);
        assertEquals(false, produto.getDisponivel());
        verify(produtoRepository).findById(1);
        verify(produtoRepository).save(produto);
        verify(produtoResponseMapper).map(produtoSalvo);
    }

    @Test
    void ativarProdutoDeveSalvarProdutoDisponivel() {
        Categoria categoria = criarCategoria(1, "Lanches", true);
        Produto produto = criarProduto(1, "Hamburguer", "Artesanal", "29.90", categoria, false);
        Produto produtoSalvo = criarProduto(1, "Hamburguer", "Artesanal", "29.90", categoria, true);
        ProdutoResponse response = criarResponse(produtoSalvo);

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(produto)).thenReturn(produtoSalvo);
        when(produtoResponseMapper.map(produtoSalvo)).thenReturn(response);

        ProdutoResponse resultado = produtoService.ativarProduto(1);

        assertSame(response, resultado);
        assertEquals(true, produto.getDisponivel());
        verify(produtoRepository).findById(1);
        verify(produtoRepository).save(produto);
        verify(produtoResponseMapper).map(produtoSalvo);
    }

    @Test
    void verificarDisponibilidadeParaVendaDeveRetornarDisponibilidadeDoProduto() {
        Categoria categoria = criarCategoria(1, "Lanches", true);
        Produto produto = criarProduto(1, "Hamburguer", "Artesanal", "29.90", categoria, false);
        ProdutoDisponibilidadeResponse response = new ProdutoDisponibilidadeResponse(1, "Hamburguer", false, new BigDecimal("29.90"));

        when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
        when(produtoDisponibilidadeResponseMapper.map(produto)).thenReturn(response);

        ProdutoDisponibilidadeResponse resultado = produtoService.verificarDisponibilidadeParaVenda(1);

        assertSame(response, resultado);
        verify(produtoRepository).findById(1);
        verify(produtoDisponibilidadeResponseMapper).map(produto);
    }

    @Test
    void ativarProdutoDeveLancarExcecaoQuandoProdutoNaoExistir() {
        when(produtoRepository.findById(99)).thenReturn(Optional.empty());

        BaseException exception = assertThrows(BaseException.class, () -> produtoService.ativarProduto(99));

        assertEquals(ErrorEnum.PRODUTO_NAO_ENCONTRADO, exception.getErrorEnum());
        verify(produtoRepository).findById(99);
        verify(produtoRepository, never()).save(any());
        verifyNoInteractions(produtoResponseMapper);
    }

    private Categoria criarCategoria(Integer id, String nome, Boolean ativo) {
        return Categoria.builder()
                .id(id)
                .nome(nome)
                .ativo(ativo)
                .build();
    }

    private Produto criarProduto(
            Integer id,
            String nome,
            String descricao,
            String preco,
            Categoria categoria,
            Boolean disponivel
    ) {
        return Produto.builder()
                .id(id)
                .nome(nome)
                .descricao(descricao)
                .preco(new BigDecimal(preco))
                .categoria(categoria)
                .disponivel(disponivel)
                .build();
    }

    private ProdutoResponse criarResponse(Produto produto) {
        Categoria categoria = produto.getCategoria();

        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                null,
                produto.getPreco(),
                categoria != null ? categoria.getId() : null,
                categoria != null ? categoria.getNome() : null,
                produto.getDisponivel()
        );
    }
}
