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
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.realtime.RealtimeEventType;
import br.com.fourkitchen.bff_restaurante.realtime.RealtimeNotifier;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestorCatalogoService {

    private final ProdutoClient produtoClient;

    private final RealtimeNotifier realtimeNotifier;

    public List<ProdutoGestorResponse> listarProdutos() {
        try {
            return produtoClient.listarProdutos()
                    .stream()
                    .sorted(Comparator.comparing(ProdutoGestorClientResponse::nome))
                    .map(this::mapearProduto)
                    .toList();
        } catch (FeignException e) {
            throw mapearErroProduto(e);
        }
    }

    public ProdutoGestorResponse criarProduto(CriarProdutoGestorRequest request) {
        try {
            ProdutoGestorResponse produto = mapearProduto(produtoClient.criarProduto(mapearProdutoRequest(request)));
            realtimeNotifier.catalogoAlterado(RealtimeEventType.PRODUTO_ALTERADO, produto.id());
            return produto;
        } catch (FeignException e) {
            throw mapearErroProduto(e);
        }
    }

    public ProdutoGestorResponse atualizarProduto(Integer id, AtualizarProdutoGestorRequest request) {
        try {
            ProdutoGestorResponse produto = mapearProduto(produtoClient.atualizarProduto(id, mapearProdutoRequest(request)));
            realtimeNotifier.catalogoAlterado(RealtimeEventType.PRODUTO_ALTERADO, produto.id());
            return produto;
        } catch (FeignException e) {
            throw mapearErroProduto(e);
        }
    }

    public ProdutoGestorResponse ativarProduto(Integer id) {
        try {
            ProdutoGestorResponse produto = mapearProduto(produtoClient.ativarProduto(id));
            realtimeNotifier.catalogoAlterado(RealtimeEventType.PRODUTO_ALTERADO, produto.id());
            return produto;
        } catch (FeignException e) {
            throw mapearErroProduto(e);
        }
    }

    public ProdutoGestorResponse desativarProduto(Integer id) {
        try {
            ProdutoGestorResponse produto = mapearProduto(produtoClient.desativarProduto(id));
            realtimeNotifier.catalogoAlterado(RealtimeEventType.PRODUTO_ALTERADO, produto.id());
            return produto;
        } catch (FeignException e) {
            throw mapearErroProduto(e);
        }
    }

    public List<CategoriaGestorResponse> listarCategorias() {
        try {
            return produtoClient.listarCategorias()
                    .stream()
                    .sorted(Comparator.comparing(CategoriaGestorClientResponse::nome))
                    .map(this::mapearCategoria)
                    .toList();
        } catch (FeignException e) {
            throw mapearErroCategoria(e);
        }
    }

    public CategoriaGestorResponse criarCategoria(CriarCategoriaGestorRequest request) {
        try {
            CategoriaGestorResponse categoria = mapearCategoria(produtoClient.criarCategoria(mapearCategoriaRequest(request)));
            realtimeNotifier.catalogoAlterado(RealtimeEventType.CATEGORIA_ALTERADA, categoria.id());
            return categoria;
        } catch (FeignException e) {
            throw mapearErroCategoria(e);
        }
    }

    public CategoriaGestorResponse atualizarCategoria(Integer id, AtualizarCategoriaGestorRequest request) {
        try {
            CategoriaGestorResponse categoria = mapearCategoria(produtoClient.atualizarCategoria(id, mapearCategoriaRequest(request)));
            realtimeNotifier.catalogoAlterado(RealtimeEventType.CATEGORIA_ALTERADA, categoria.id());
            return categoria;
        } catch (FeignException e) {
            throw mapearErroCategoria(e);
        }
    }

    public CategoriaGestorResponse ativarCategoria(Integer id) {
        try {
            CategoriaGestorResponse categoria = mapearCategoria(produtoClient.ativarCategoria(id));
            realtimeNotifier.catalogoAlterado(RealtimeEventType.CATEGORIA_ALTERADA, categoria.id());
            return categoria;
        } catch (FeignException e) {
            throw mapearErroCategoria(e);
        }
    }

    public CategoriaGestorResponse desativarCategoria(Integer id) {
        try {
            CategoriaGestorResponse categoria = mapearCategoria(produtoClient.desativarCategoria(id));
            realtimeNotifier.catalogoAlterado(RealtimeEventType.CATEGORIA_ALTERADA, categoria.id());
            return categoria;
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
                produto.imagem(),
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
                categoria.imagem(),
                categoria.ativo()
        );
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
