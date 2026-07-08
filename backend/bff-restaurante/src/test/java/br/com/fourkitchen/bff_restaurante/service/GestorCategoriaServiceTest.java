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
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorCategoriaServiceTest {

    private static final String AUTHORIZATION = "Bearer token";

    @Mock
    private CategoriaClient categoriaClient;

    @InjectMocks
    private GestorCategoriaService gestorCategoriaService;

    @Test
    void listarCategoriasDeveRetornarCategoriasOrdenadasPorNome() {
        CategoriaGestorClientResponse bebidas = criarCategoria(2, "Bebidas", true);
        CategoriaGestorClientResponse lanches = criarCategoria(1, "Lanches", true);

        when(categoriaClient.listarCategorias()).thenReturn(List.of(lanches, bebidas));

        List<CategoriaGestorResponse> resultado = gestorCategoriaService.listarCategorias(AUTHORIZATION);

        assertEquals("Bebidas", resultado.get(0).nome());
        assertEquals("Lanches", resultado.get(1).nome());
        verify(categoriaClient).listarCategorias();
    }

    @Test
    void criarCategoriaDeveDelegarParaMsProdutos() {
        CriarCategoriaGestorRequest request = new CriarCategoriaGestorRequest(
                "Lanches",
                "Sanduiches",
                "base64"
        );

        when(categoriaClient.criarCategoria(any(CriarCategoriaClientRequest.class)))
                .thenReturn(criarCategoria(1, "Lanches", true));

        CategoriaGestorResponse resultado = gestorCategoriaService.criarCategoria(request, AUTHORIZATION);

        assertEquals("Lanches", resultado.nome());

        ArgumentCaptor<CriarCategoriaClientRequest> captor = ArgumentCaptor.forClass(CriarCategoriaClientRequest.class);
        verify(categoriaClient).criarCategoria(captor.capture());
        assertEquals("base64", captor.getValue().imagem());
    }

    @Test
    void atualizarCategoriaDeveDelegarParaMsProdutos() {
        AtualizarCategoriaGestorRequest request = new AtualizarCategoriaGestorRequest(
                "Lanches",
                "Sanduiches",
                null
        );

        when(categoriaClient.atualizarCategoria(eq(1), any(AtualizarCategoriaClientRequest.class)))
                .thenReturn(criarCategoria(1, "Lanches", true));

        CategoriaGestorResponse resultado = gestorCategoriaService.atualizarCategoria(1, request, AUTHORIZATION);

        assertEquals("Lanches", resultado.nome());
        verify(categoriaClient).atualizarCategoria(eq(1), any(AtualizarCategoriaClientRequest.class));
    }

    @Test
    void ativarCategoriaDeveDelegarParaMsProdutos() {
        when(categoriaClient.ativarCategoria(1)).thenReturn(criarCategoria(1, "Lanches", true));

        CategoriaGestorResponse resultado = gestorCategoriaService.ativarCategoria(1, AUTHORIZATION);

        assertEquals(true, resultado.ativo());
        verify(categoriaClient).ativarCategoria(1);
    }

    @Test
    void atualizarCategoriaDeveMapearNomeDuplicado() {
        AtualizarCategoriaGestorRequest request = new AtualizarCategoriaGestorRequest(
                "Lanches",
                "Sanduiches",
                null
        );

        when(categoriaClient.atualizarCategoria(eq(1), any(AtualizarCategoriaClientRequest.class)))
                .thenThrow(feignException(409));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> gestorCategoriaService.atualizarCategoria(1, request, AUTHORIZATION)
        );

        assertEquals(ErrorEnum.CATEGORIA_NOME_DUPLICADO, exception.getErrorEnum());
    }

    @Test
    void listarCategoriasDeveLancarTokenInvalidoQuandoAuthorizationNaoForBearer() {
        BaseException exception = assertThrows(
                BaseException.class,
                () -> gestorCategoriaService.listarCategorias("token")
        );

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
        verifyNoInteractions(categoriaClient);
    }

    private CategoriaGestorClientResponse criarCategoria(Integer id, String nome, Boolean ativo) {
        return new CategoriaGestorClientResponse(id, nome, "Descricao", null, ativo);
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/categorias",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null
        );

        Response response = Response.builder()
                .status(status)
                .reason("Erro")
                .request(request)
                .build();

        return FeignException.errorStatus("categorias", response);
    }
}
