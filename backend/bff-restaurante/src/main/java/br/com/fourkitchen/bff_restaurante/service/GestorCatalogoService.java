package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaGestorClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaGestorRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoGestorClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarCategoriaGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarProdutoGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarCategoriaGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarProdutoGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaGestorResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ProdutoGestorResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ProdutoGestorPaginadoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaGestorPaginadaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaOpcaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GestorCatalogoService {

    private final ProdutoClient produtoClient;

    public ProdutoGestorPaginadoResponse listarProdutos(Integer page, Integer size, String busca, Integer categoriaId) {
        try {
            int pagina = normalizarPagina(page);
            int tamanho = normalizarTamanho(size);
            var resposta = produtoClient.listarProdutos(
                    pagina,
                    tamanho,
                    normalizarBusca(busca),
                    normalizarCategoria(categoriaId)
            );
            return new ProdutoGestorPaginadoResponse(
                    resposta.content().stream().map(this::mapearProduto).toList(),
                    resposta.page(),
                    resposta.size(),
                    resposta.totalElements(),
                    resposta.totalPages(),
                    resposta.first(),
                    resposta.last()
            );
        } catch (FeignException e) {
            throw mapearErroProduto(e);
        }
    }

    public ProdutoGestorResponse criarProduto(CriarProdutoGestorRequest request) {
        try {
            return mapearProduto(produtoClient.criarProduto(mapearProdutoRequest(request)));
        } catch (FeignException e) {
            throw mapearErroProduto(e);
        }
    }

    public ProdutoGestorResponse atualizarProduto(Integer id, AtualizarProdutoGestorRequest request) {
        try {
            return mapearProduto(produtoClient.atualizarProduto(id, mapearProdutoRequest(request)));
        } catch (FeignException e) {
            throw mapearErroProduto(e);
        }
    }

    public ProdutoGestorResponse ativarProduto(Integer id) {
        try {
            return mapearProduto(produtoClient.ativarProduto(id));
        } catch (FeignException e) {
            throw mapearErroProduto(e);
        }
    }

    public ProdutoGestorResponse desativarProduto(Integer id) {
        try {
            return mapearProduto(produtoClient.desativarProduto(id));
        } catch (FeignException e) {
            throw mapearErroProduto(e);
        }
    }

    public CategoriaGestorPaginadaResponse listarCategorias(Integer page, Integer size, String busca, Boolean ativo) {
        try {
            int pagina = normalizarPagina(page);
            int tamanho = normalizarTamanho(size);
            var resposta = produtoClient.listarCategorias(pagina, tamanho, normalizarBusca(busca), ativo);
            return new CategoriaGestorPaginadaResponse(
                    resposta.content().stream().map(this::mapearCategoria).toList(),
                    resposta.page(),
                    resposta.size(),
                    resposta.totalElements(),
                    resposta.totalPages(),
                    resposta.first(),
                    resposta.last()
            );
        } catch (FeignException e) {
            throw mapearErroCategoria(e);
        }
    }

    public List<CategoriaOpcaoResponse> listarOpcoesCategorias() {
        try {
            return produtoClient.listarOpcoesCategorias()
                    .stream()
                    .map(categoria -> new CategoriaOpcaoResponse(
                            categoria.id(),
                            categoria.nome(),
                            categoria.ativo()
                    ))
                    .toList();
        } catch (FeignException e) {
            throw mapearErroCategoria(e);
        }
    }

    public CategoriaGestorResponse criarCategoria(CriarCategoriaGestorRequest request) {
        try {
            return mapearCategoria(produtoClient.criarCategoria(mapearCategoriaRequest(request)));
        } catch (FeignException e) {
            throw mapearErroCategoria(e);
        }
    }

    public CategoriaGestorResponse atualizarCategoria(Integer id, AtualizarCategoriaGestorRequest request) {
        try {
            return mapearCategoria(produtoClient.atualizarCategoria(id, mapearCategoriaRequest(request)));
        } catch (FeignException e) {
            throw mapearErroCategoria(e);
        }
    }

    public CategoriaGestorResponse ativarCategoria(Integer id) {
        try {
            return mapearCategoria(produtoClient.ativarCategoria(id));
        } catch (FeignException e) {
            throw mapearErroCategoria(e);
        }
    }

    public CategoriaGestorResponse desativarCategoria(Integer id) {
        try {
            return mapearCategoria(produtoClient.desativarCategoria(id));
        } catch (FeignException e) {
            throw mapearErroCategoria(e);
        }
    }

    private ProdutoGestorRequest mapearProdutoRequest(CriarProdutoGestorRequest request) {
        return new ProdutoGestorRequest(
                request.nome(),
                request.descricao(),
                request.imagem(),
                request.preco(),
                request.categoriaId()
        );
    }

    private ProdutoGestorRequest mapearProdutoRequest(AtualizarProdutoGestorRequest request) {
        return new ProdutoGestorRequest(
                request.nome(),
                request.descricao(),
                request.imagem(),
                request.preco(),
                request.categoriaId()
        );
    }

    private CategoriaGestorRequest mapearCategoriaRequest(CriarCategoriaGestorRequest request) {
        return new CategoriaGestorRequest(
                request.nome(),
                request.descricao(),
                request.imagem()
        );
    }

    private CategoriaGestorRequest mapearCategoriaRequest(AtualizarCategoriaGestorRequest request) {
        return new CategoriaGestorRequest(
                request.nome(),
                request.descricao(),
                request.imagem()
        );
    }

    private ProdutoGestorResponse mapearProduto(ProdutoGestorClientResponse produto) {
        return new ProdutoGestorResponse(
                produto.id(),
                produto.nome(),
                produto.descricao(),
                produto.imagemUrl(),
                produto.preco(),
                produto.categoriaId(),
                produto.categoria(),
                produto.disponivel()
        );
    }

    private CategoriaGestorResponse mapearCategoria(CategoriaGestorClientResponse categoria) {
        return new CategoriaGestorResponse(
                categoria.id(),
                categoria.nome(),
                categoria.descricao(),
                categoria.imagemUrl(),
                categoria.ativo()
        );
    }

    private int normalizarPagina(Integer page) {
        return page == null ? 0 : Math.max(0, page);
    }

    private int normalizarTamanho(Integer size) {
        return size == null ? 10 : Math.min(Math.max(1, size), 50);
    }

    private String normalizarBusca(String busca) {
        return busca == null || busca.isBlank() ? null : busca.trim();
    }

    private Integer normalizarCategoria(Integer categoriaId) {
        return categoriaId == null || categoriaId <= 0 ? null : categoriaId;
    }

    private BaseException mapearErroProduto(FeignException e) {
        return switch (e.status()) {
            case 400 -> new BaseException(ErrorEnum.DADOS_INVALIDOS);
            case 404 -> new BaseException(ErrorEnum.PRODUTO_NAO_ENCONTRADO);
            default -> new BaseException(ErrorEnum.MS_PRODUTOS_INDISPONIVEL);
        };
    }

    private BaseException mapearErroCategoria(FeignException e) {
        return switch (e.status()) {
            case 400 -> new BaseException(ErrorEnum.DADOS_INVALIDOS);
            case 404 -> new BaseException(ErrorEnum.CATEGORIA_NAO_ENCONTRADA);
            case 409 -> new BaseException(ErrorEnum.CATEGORIA_NOME_DUPLICADO);
            default -> new BaseException(ErrorEnum.MS_PRODUTOS_INDISPONIVEL);
        };
    }
}
