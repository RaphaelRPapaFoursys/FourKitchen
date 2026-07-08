package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.produtos.CategoriaClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.AtualizarCategoriaClientRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaGestorClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CriarCategoriaClientRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarCategoriaGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarCategoriaGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaGestorResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestorCategoriaService {

    private final CategoriaClient categoriaClient;

    public List<CategoriaGestorResponse> listarCategorias(String authorization) {
        validarAuthorization(authorization);

        try {
            return categoriaClient.listarCategorias()
                    .stream()
                    .sorted(Comparator.comparing(CategoriaGestorClientResponse::nome))
                    .map(this::mapearCategoria)
                    .toList();
        } catch (FeignException e) {
            throw mapearErroMsCategorias(e);
        }
    }

    public CategoriaGestorResponse criarCategoria(CriarCategoriaGestorRequest request, String authorization) {
        validarAuthorization(authorization);

        CriarCategoriaClientRequest clientRequest = new CriarCategoriaClientRequest(
                request.nome(),
                request.descricao(),
                request.imagem()
        );

        try {
            return mapearCategoria(categoriaClient.criarCategoria(clientRequest));
        } catch (FeignException e) {
            throw mapearErroMsCategorias(e);
        }
    }

    public CategoriaGestorResponse atualizarCategoria(
            Integer id,
            AtualizarCategoriaGestorRequest request,
            String authorization
    ) {
        validarAuthorization(authorization);

        AtualizarCategoriaClientRequest clientRequest = new AtualizarCategoriaClientRequest(
                request.nome(),
                request.descricao(),
                request.imagem()
        );

        try {
            return mapearCategoria(categoriaClient.atualizarCategoria(id, clientRequest));
        } catch (FeignException e) {
            throw mapearErroMsCategorias(e);
        }
    }

    public CategoriaGestorResponse ativarCategoria(Integer id, String authorization) {
        validarAuthorization(authorization);

        try {
            return mapearCategoria(categoriaClient.ativarCategoria(id));
        } catch (FeignException e) {
            throw mapearErroMsCategorias(e);
        }
    }

    public CategoriaGestorResponse desativarCategoria(Integer id, String authorization) {
        validarAuthorization(authorization);

        try {
            return mapearCategoria(categoriaClient.desativarCategoria(id));
        } catch (FeignException e) {
            throw mapearErroMsCategorias(e);
        }
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

    private void validarAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }
    }

    private BaseException mapearErroMsCategorias(FeignException e) {
        return switch (e.status()) {
            case 400 -> new BaseException(ErrorEnum.DADOS_INVALIDOS);
            case 404 -> new BaseException(ErrorEnum.CATEGORIA_NAO_ENCONTRADA);
            case 409 -> new BaseException(ErrorEnum.CATEGORIA_NOME_DUPLICADO);
            default -> new BaseException(ErrorEnum.MS_PRODUTOS_INDISPONIVEL);
        };
    }
}
