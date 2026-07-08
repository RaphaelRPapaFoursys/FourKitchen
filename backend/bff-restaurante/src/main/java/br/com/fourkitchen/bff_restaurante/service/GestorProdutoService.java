package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.AtualizarProdutoClientRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CriarProdutoClientRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoGestorClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarProdutoGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarProdutoGestorRequest;
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

    public List<ProdutoGestorResponse> listarProdutos(String authorization) {
        validarAuthorization(authorization);

        try {
            return produtoClient.listarProdutos()
                    .stream()
                    .sorted(Comparator.comparing(ProdutoGestorClientResponse::nome))
                    .map(this::mapearProduto)
                    .toList();
        } catch (FeignException e) {
            throw mapearErroMsProdutos(e);
        }
    }

    public ProdutoGestorResponse criarProduto(CriarProdutoGestorRequest request, String authorization) {
        validarAuthorization(authorization);

        CriarProdutoClientRequest clientRequest = new CriarProdutoClientRequest(
                request.nome(),
                request.descricao(),
                request.imagem(),
                request.preco(),
                request.categoriaId()
        );

        try {
            return mapearProduto(produtoClient.criarProduto(clientRequest));
        } catch (FeignException e) {
            throw mapearErroCriarProduto(e);
        }
    }

    public ProdutoGestorResponse atualizarProduto(
            Integer id,
            AtualizarProdutoGestorRequest request,
            String authorization
    ) {
        validarAuthorization(authorization);

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

    public ProdutoGestorResponse ativarProduto(Integer id, String authorization) {
        validarAuthorization(authorization);

        try {
            return mapearProduto(produtoClient.ativarProduto(id));
        } catch (FeignException e) {
            throw mapearErroMsProdutos(e);
        }
    }

    public ProdutoGestorResponse desativarProduto(Integer id, String authorization) {
        validarAuthorization(authorization);

        try {
            return mapearProduto(produtoClient.desativarProduto(id));
        } catch (FeignException e) {
            throw mapearErroMsProdutos(e);
        }
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

    private void validarAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }
    }

    private BaseException mapearErroMsProdutos(FeignException e) {
        return switch (e.status()) {
            case 400 -> new BaseException(ErrorEnum.DADOS_INVALIDOS);
            case 404 -> new BaseException(ErrorEnum.PRODUTO_NAO_ENCONTRADO);
            default -> new BaseException(ErrorEnum.MS_PRODUTOS_INDISPONIVEL);
        };
    }

    private BaseException mapearErroCriarProduto(FeignException e) {
        return switch (e.status()) {
            case 400 -> new BaseException(ErrorEnum.DADOS_INVALIDOS);
            case 404 -> new BaseException(ErrorEnum.CATEGORIA_NAO_ENCONTRADA);
            default -> new BaseException(ErrorEnum.MS_PRODUTOS_INDISPONIVEL);
        };
    }
}
