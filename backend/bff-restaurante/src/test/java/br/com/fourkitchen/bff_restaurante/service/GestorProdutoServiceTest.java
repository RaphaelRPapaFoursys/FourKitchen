package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CriarProdutoClientRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarCategoriaRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarProdutoRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaGestorResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ProdutoGestorResponse;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorProdutoServiceTest {

    @Mock
    private ProdutoClient produtoClient;

    @InjectMocks
    private GestorProdutoService gestorProdutoService;

    @Test
    void listarProdutosDeveRetornarProdutosOrdenadosPorNome() {
        ProdutoClientResponse risoto = criarProduto(2, "Risoto", true);
        ProdutoClientResponse agua = criarProduto(1, "Agua", true);

        when(produtoClient.listarProdutos()).thenReturn(List.of(risoto, agua));

        List<ProdutoGestorResponse> resultado = gestorProdutoService.listarProdutos();

        assertEquals(2, resultado.size());
        assertEquals("Agua", resultado.get(0).nome());
        assertEquals("Risoto", resultado.get(1).nome());
        verify(produtoClient).listarProdutos();
    }

    @Test
    void criarProdutoDeveDelegarParaMsProdutos() {
        CriarProdutoRequest request = new CriarProdutoRequest(
                "Pudim",
                "Fatia individual",
                "base64",
                BigDecimal.valueOf(18.90),
                4,
                false
        );
        ProdutoClientResponse produtoCriado = criarProduto(10, "Pudim", true);

        when(produtoClient.criarProduto(any(CriarProdutoClientRequest.class))).thenReturn(produtoCriado);

        ProdutoGestorResponse resultado = gestorProdutoService.criarProduto(request);

        assertEquals(10, resultado.id());
        assertEquals("Pudim", resultado.nome());

        ArgumentCaptor<CriarProdutoClientRequest> requestCaptor =
                ArgumentCaptor.forClass(CriarProdutoClientRequest.class);
        verify(produtoClient).criarProduto(requestCaptor.capture());
        assertEquals("base64", requestCaptor.getValue().imagem());
        assertEquals(4, requestCaptor.getValue().categoriaId());
        assertEquals(false, requestCaptor.getValue().disponivel());
    }

    @Test
    void desativarProdutoDeveRetornarProdutoAtualizado() {
        ProdutoClientResponse produtoDesativado = criarProduto(1, "Agua", false);

        when(produtoClient.desativarProduto(1)).thenReturn(produtoDesativado);

        ProdutoGestorResponse resultado = gestorProdutoService.desativarProduto(1);

        assertEquals(1, resultado.id());
        assertEquals(false, resultado.disponivel());
        verify(produtoClient).desativarProduto(1);
    }

    @Test
    void listarCategoriasDeveRetornarCategoriasOrdenadasPorNome() {
        CategoriaClientResponse sobremesas = new CategoriaClientResponse(2, "Sobremesas", null, true);
        CategoriaClientResponse bebidas = new CategoriaClientResponse(1, "Bebidas", null, true);

        when(produtoClient.listarCategorias()).thenReturn(List.of(sobremesas, bebidas));

        List<CategoriaGestorResponse> resultado = gestorProdutoService.listarCategorias();

        assertEquals("Bebidas", resultado.get(0).nome());
        assertEquals("Sobremesas", resultado.get(1).nome());
    }

    @Test
    void criarCategoriaDeveDelegarParaMsProdutos() {
        CriarCategoriaRequest request = new CriarCategoriaRequest("Entradas", "Porcoes e aperitivos");
        CategoriaClientResponse categoria = new CategoriaClientResponse(1, "Entradas", "Porcoes e aperitivos", true);

        when(produtoClient.criarCategoria(any())).thenReturn(categoria);

        CategoriaGestorResponse resultado = gestorProdutoService.criarCategoria(request);

        assertSame("Entradas", resultado.nome());
        verify(produtoClient).criarCategoria(any());
    }

    @Test
    void atualizarProdutoDeveMapearNaoEncontrado() {
        when(produtoClient.atualizarProduto(any(), any())).thenThrow(feignException(404));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> gestorProdutoService.atualizarProduto(99, new br.com.fourkitchen.bff_restaurante.dto.request.AtualizarProdutoRequest(
                        "Pudim",
                        null,
                        null,
                        BigDecimal.TEN,
                        1
                ))
        );

        assertEquals(ErrorEnum.PRODUTO_NAO_ENCONTRADO, exception.getErrorEnum());
    }

    @Test
    void criarCategoriaDeveMapearNomeDuplicado() {
        when(produtoClient.criarCategoria(any())).thenThrow(feignException(409));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> gestorProdutoService.criarCategoria(new CriarCategoriaRequest("Bebidas", null))
        );

        assertEquals(ErrorEnum.CATEGORIA_NOME_DUPLICADO, exception.getErrorEnum());
    }

    private ProdutoClientResponse criarProduto(Integer id, String nome, Boolean disponivel) {
        return new ProdutoClientResponse(
                id,
                nome,
                "Descricao",
                null,
                BigDecimal.valueOf(29.90),
                1,
                "Categoria",
                disponivel
        );
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/produtos",
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

        return FeignException.errorStatus("produtos", response);
    }
}
