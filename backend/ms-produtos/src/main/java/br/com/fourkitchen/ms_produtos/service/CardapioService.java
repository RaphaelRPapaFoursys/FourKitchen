package br.com.fourkitchen.ms_produtos.service;

import br.com.fourkitchen.ms_produtos.dto.response.CategoriaCardapioResponse;
import br.com.fourkitchen.ms_produtos.dto.response.CategoriaCardapioResumoResponse;
import br.com.fourkitchen.ms_produtos.dto.response.CardapioPaginadoResponse;
import br.com.fourkitchen.ms_produtos.dto.response.ProdutoCardapioResponse;
import br.com.fourkitchen.ms_produtos.mapper.CategoriaCardapioMapperSource;
import br.com.fourkitchen.ms_produtos.mapper.CategoriaCardapioResponseMapper;
import br.com.fourkitchen.ms_produtos.mapper.ProdutoCardapioResponseMapper;
import br.com.fourkitchen.ms_produtos.repository.ProdutoCardapioProjection;
import br.com.fourkitchen.ms_produtos.repository.ProdutoRepository;
import br.com.fourkitchen.ms_produtos.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CardapioService {

    private final ProdutoRepository produtoRepository;

    private final CategoriaRepository categoriaRepository;

    private final ProdutoCardapioResponseMapper produtoCardapioResponseMapper;

    private final CategoriaCardapioResponseMapper categoriaCardapioResponseMapper;

    public List<CategoriaCardapioResponse> buscarCardapio() {
        Map<Integer, ProdutoCardapioProjection> categoriasPorId = new LinkedHashMap<>();
        Map<Integer, List<ProdutoCardapioResponse>> produtosPorCategoria = new LinkedHashMap<>();

        produtoRepository.buscarProdutosDisponiveisParaCardapio()
                .forEach(produto -> adicionarProdutoAoCardapio(produto, categoriasPorId, produtosPorCategoria));

        return produtosPorCategoria.entrySet()
                .stream()
                .map(entry -> mapearCategoria(entry.getKey(), entry.getValue(), categoriasPorId))
                .map(categoriaCardapioResponseMapper::map)
                .toList();
    }

    public List<CategoriaCardapioResumoResponse> buscarCategoriasAtivas() {
        return categoriaRepository.findByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(categoria -> new CategoriaCardapioResumoResponse(
                        categoria.getId(),
                        categoria.getNome(),
                        categoria.getDescricao()
                ))
                .toList();
    }

    public CardapioPaginadoResponse buscarCardapioPaginado(Integer categoriaId, Pageable pageable) {
        Page<ProdutoCardapioProjection> pagina = produtoRepository.buscarProdutosDisponiveisParaCardapio(categoriaId, pageable);
        List<CategoriaCardapioResponse> categorias = agruparProdutos(pagina.getContent());

        return new CardapioPaginadoResponse(
                categorias,
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isFirst(),
                pagina.isLast()
        );
    }

    private List<CategoriaCardapioResponse> agruparProdutos(List<ProdutoCardapioProjection> produtos) {
        Map<Integer, ProdutoCardapioProjection> categoriasPorId = new LinkedHashMap<>();
        Map<Integer, List<ProdutoCardapioResponse>> produtosPorCategoria = new LinkedHashMap<>();

        produtos.forEach(produto -> adicionarProdutoAoCardapio(produto, categoriasPorId, produtosPorCategoria));

        return produtosPorCategoria.entrySet()
                .stream()
                .map(entry -> mapearCategoria(entry.getKey(), entry.getValue(), categoriasPorId))
                .map(categoriaCardapioResponseMapper::map)
                .toList();
    }

    private void adicionarProdutoAoCardapio(
            ProdutoCardapioProjection produto,
            Map<Integer, ProdutoCardapioProjection> categoriasPorId,
            Map<Integer, List<ProdutoCardapioResponse>> produtosPorCategoria
    ) {
        Integer categoriaId = produto.getCategoriaId();
        categoriasPorId.putIfAbsent(categoriaId, produto);
        produtosPorCategoria
                .computeIfAbsent(categoriaId, id -> new ArrayList<>())
                .add(produtoCardapioResponseMapper.map(produto));
    }

    private CategoriaCardapioMapperSource mapearCategoria(
            Integer categoriaId,
            List<ProdutoCardapioResponse> produtos,
            Map<Integer, ProdutoCardapioProjection> categoriasPorId
    ) {
        ProdutoCardapioProjection categoria = categoriasPorId.get(categoriaId);
        return new CategoriaCardapioMapperSource(
                categoria.getCategoriaId(),
                categoria.getCategoriaNome(),
                categoria.getCategoriaDescricao(),
                produtos
        );
    }
}
