package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.AtualizarProdutoClientRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CriarCategoriaClientRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CriarProdutoClientRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarProdutoRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarCategoriaRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarProdutoRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaGestorResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ProdutoGestorResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestorProdutoService {

    private final ProdutoClient produtoClient;

    public List<ProdutoGestorResponse> listarProdutos() {
        try {
            return produtoClient.listarProdutos()
                    .stream()
                    .sorted(Comparator.comparing(ProdutoClientResponse::nome))
                    .map(this::mapearProduto)
                    .toList();
        } catch (FeignException e) {
            throw mapearErroMsProdutos(e);
        }
    }

    public ProdutoGestorResponse criarProduto(CriarProdutoRequest request) {
        CriarProdutoClientRequest clientRequest = new CriarProdutoClientRequest(
                request.nome(),
                request.descricao(),
                request.imagem(),
                request.preco(),
                request.categoriaId(),
                request.disponivel()
        );

        try {
            return mapearProduto(produtoClient.criarProduto(clientRequest));
        } catch (FeignException e) {
            throw mapearErroMsProdutos(e);
        }
    }

    public ProdutoGestorResponse atualizarProduto(Integer id, AtualizarProdutoRequest request) {
        AtualizarProdutoClientRequest clientRequest = new AtualizarProdutoClientRequest(
                request.nome(),
                request.descricao(),
                request.imagem(),
                request.preco(),
                request.categoriaId()
        );

        try {
            return mapearProduto(produtoClient.atualizarProduto(id, clientRequest));
        } catch (FeignException e) {
            throw mapearErroMsProdutos(e);
        }
    }

    public ProdutoGestorResponse ativarProduto(Integer id) {
        try {
            return mapearProduto(produtoClient.ativarProduto(id));
        } catch (FeignException e) {
            throw mapearErroMsProdutos(e);
        }
    }

    public ProdutoGestorResponse desativarProduto(Integer id) {
        try {
            return mapearProduto(produtoClient.desativarProduto(id));
        } catch (FeignException e) {
            throw mapearErroMsProdutos(e);
        }
    }

    public List<CategoriaGestorResponse> listarCategorias() {
        try {
            return produtoClient.listarCategorias()
                    .stream()
                    .sorted(Comparator.comparing(CategoriaClientResponse::nome))
                    .map(this::mapearCategoria)
                    .toList();
        } catch (FeignException e) {
            throw mapearErroMsProdutos(e);
        }
    }

    public CategoriaGestorResponse criarCategoria(CriarCategoriaRequest request) {
        CriarCategoriaClientRequest clientRequest = new CriarCategoriaClientRequest(
                request.nome(),
                request.descricao()
        );

        try {
            return mapearCategoria(produtoClient.criarCategoria(clientRequest));
        } catch (FeignException e) {
            throw mapearErroMsProdutos(e);
        }
    }

    private ProdutoGestorResponse mapearProduto(ProdutoClientResponse produto) {
        return new ProdutoGestorResponse(
                produto.id(),
                produto.nome(),
                produto.descricao(),
                produto.imagem(),
                produto.preco(),
                produto.categoriaId(),
                produto.categoria(),
                produto.disponivel()
        );
    }

    private CategoriaGestorResponse mapearCategoria(CategoriaClientResponse categoria) {
        return new CategoriaGestorResponse(
                categoria.id(),
                categoria.nome(),
                categoria.descricao(),
                categoria.ativo()
        );
    }

    private BaseException mapearErroMsProdutos(FeignException e) {
        return switch (e.status()) {
            case 400 -> new BaseException(ErrorEnum.DADOS_INVALIDOS);
            case 404 -> new BaseException(ErrorEnum.PRODUTO_NAO_ENCONTRADO);
            case 409 -> new BaseException(ErrorEnum.CATEGORIA_NOME_DUPLICADO);
            default -> new BaseException(ErrorEnum.MS_PRODUTOS_INDISPONIVEL);
        };
    }
}
