package br.com.fourkitchen.ms_produtos.service;

import br.com.fourkitchen.ms_produtos.dto.response.CategoriaCardapioResponse;
import br.com.fourkitchen.ms_produtos.dto.response.ProdutoCardapioResponse;
import br.com.fourkitchen.ms_produtos.mapper.CategoriaCardapioMapperSource;
import br.com.fourkitchen.ms_produtos.mapper.CategoriaCardapioResponseMapper;
import br.com.fourkitchen.ms_produtos.mapper.ProdutoCardapioResponseMapper;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import br.com.fourkitchen.ms_produtos.model.Produto;
import br.com.fourkitchen.ms_produtos.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CardapioService {

    private final ProdutoRepository produtoRepository;

    private final ProdutoCardapioResponseMapper produtoCardapioResponseMapper;

    private final CategoriaCardapioResponseMapper categoriaCardapioResponseMapper;

    public List<CategoriaCardapioResponse> buscarCardapio() {
        Map<Integer, Categoria> categoriasPorId = new LinkedHashMap<>();
        Map<Integer, List<ProdutoCardapioResponse>> produtosPorCategoria = new LinkedHashMap<>();

        produtoRepository.buscarProdutosDisponiveisParaCardapio()
                .forEach(produto -> adicionarProdutoAoCardapio(produto, categoriasPorId, produtosPorCategoria));

        return produtosPorCategoria.entrySet()
                .stream()
                .map(entry -> new CategoriaCardapioMapperSource(categoriasPorId.get(entry.getKey()), entry.getValue()))
                .map(categoriaCardapioResponseMapper::map)
                .toList();
    }

    private void adicionarProdutoAoCardapio(
            Produto produto,
            Map<Integer, Categoria> categoriasPorId,
            Map<Integer, List<ProdutoCardapioResponse>> produtosPorCategoria
    ) {
        Categoria categoria = produto.getCategoria();

        categoriasPorId.putIfAbsent(categoria.getId(), categoria);
        produtosPorCategoria
                .computeIfAbsent(categoria.getId(), id -> new ArrayList<>())
                .add(produtoCardapioResponseMapper.map(produto));
    }
}
