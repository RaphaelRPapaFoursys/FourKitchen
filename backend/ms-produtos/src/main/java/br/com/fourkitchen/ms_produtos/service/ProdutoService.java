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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    private final CategoriaRepository categoriaRepository;

    private final ProdutoResponseMapper produtoResponseMapper;

    private final ProdutoDisponibilidadeResponseMapper produtoDisponibilidadeResponseMapper;

    private final CriarProdutoRequestMapper criarProdutoRequestMapper;

    private final AtualizarProdutoRequestMapper atualizarProdutoRequestMapper;

    public List<ProdutoResponse> listarProdutos() {
        return produtoRepository.findAll()
                .stream()
                .map(produtoResponseMapper::map)
                .toList();
    }

    public List<ProdutoResponse> listarProdutosDisponiveis() {
        return produtoRepository.findByDisponivelTrue()
                .stream()
                .map(produtoResponseMapper::map)
                .toList();
    }

    public ProdutoResponse criarProduto(CriarProdutoRequest request) {
        validarPreco(request.preco());
        Categoria categoria = buscarCategoriaAtiva(request.categoriaId());

        Produto produto = criarProdutoRequestMapper.map(request);
        produto.setCategoria(categoria);
        produto.setDisponivel(request.disponivel() == null ? Boolean.TRUE : request.disponivel());

        Produto produtoSalvo = produtoRepository.save(produto);

        return produtoResponseMapper.map(produtoSalvo);
    }

    public ProdutoResponse atualizarProduto(Integer id, AtualizarProdutoRequest request) {
        validarPreco(request.preco());

        Produto produto = buscarPorId(id);
        Categoria categoria = buscarCategoriaAtiva(request.categoriaId());
        atualizarProdutoRequestMapper.map(request, produto);
        produto.setCategoria(categoria);

        Produto produtoSalvo = produtoRepository.save(produto);

        return produtoResponseMapper.map(produtoSalvo);
    }

    public ProdutoResponse ativarProduto(Integer id) {
        Produto produto = buscarPorId(id);
        produto.setDisponivel(true);

        Produto produtoSalvo = produtoRepository.save(produto);

        return produtoResponseMapper.map(produtoSalvo);
    }

    public ProdutoResponse desativarProduto(Integer id) {
        Produto produto = buscarPorId(id);
        produto.setDisponivel(false);

        Produto produtoSalvo = produtoRepository.save(produto);

        return produtoResponseMapper.map(produtoSalvo);
    }

    // Verifica se o produto existe no banco de dados e retorna suas informações
    public ProdutoDisponibilidadeResponse verificarDisponibilidadeParaVenda(Integer id) {
        Produto produto = buscarPorId(id);

        return produtoDisponibilidadeResponseMapper.map(produto);
    }

    private Produto buscarPorId(Integer id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorEnum.PRODUTO_NAO_ENCONTRADO));
    }

    private Categoria buscarCategoriaAtiva(Integer categoriaId) {
        if (categoriaId == null) {
            throw new BaseException(ErrorEnum.CATEGORIA_NAO_ENCONTRADA);
        }

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new BaseException(ErrorEnum.CATEGORIA_NAO_ENCONTRADA));

        if (!Boolean.TRUE.equals(categoria.getAtivo())) {
            throw new BaseException(ErrorEnum.CATEGORIA_INATIVA);
        }

        return categoria;
    }

    private void validarPreco(BigDecimal preco) {
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(ErrorEnum.PRECO_INVALIDO);
        }
    }
}
