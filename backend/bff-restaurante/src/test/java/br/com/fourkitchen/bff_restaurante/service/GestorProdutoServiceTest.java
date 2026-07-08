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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorProdutoServiceTest {

    private static final String AUTHORIZATION = "Bearer token";

    @Mock
    private ProdutoClient produtoClient;

    @InjectMocks
    private GestorProdutoService gestorProdutoService;

    @Test
    void listarProdutosDeveRetornarProdutosOrdenadosPorNome() {
        ProdutoGestorClientResponse pizza = criarProduto(2, "Pizza", true);
        ProdutoGestorClientResponse hamburguer = criarProduto(1, "Hamburguer", true);

        when(produtoClient.listarProdutos()).thenReturn(List.of(pizza, hamburguer));

        List<ProdutoGestorResponse> resultado = gestorProdutoService.listarProdutos(AUTHORIZATION);

        assertEquals("Hamburguer", resultado.get(0).nome());
        assertEquals("Pizza", resultado.get(1).nome());
        verify(produtoClient).listarProdutos();
    }

    @Test
    void criarProdutoDeveDelegarParaMsProdutos() {
        CriarProdutoGestorRequest request = new CriarProdutoGestorRequest(
                "Hamburguer",
                "Artesanal",
                "base64",
                new BigDecimal("29.90"),
                1
        );
        ProdutoGestorClientResponse produtoCriado = criarProduto(1, "Hamburguer", true);

        when(produtoClient.criarProduto(any(CriarProdutoClientRequest.class))).thenReturn(produtoCriado);

        ProdutoGestorResponse resultado = gestorProdutoService.criarProduto(request, AUTHORIZATION);

        assertEquals("Hamburguer", resultado.nome());

        ArgumentCaptor<CriarProdutoClientRequest> captor = ArgumentCaptor.forClass(CriarProdutoClientRequest.class);
        verify(produtoClient).criarProduto(captor.capture());
        assertEquals("base64", captor.getValue().imagem());
        assertEquals(new BigDecimal("29.90"), captor.getValue().preco());
    }

    @Test
    void atualizarProdutoDeveDelegarParaMsProdutos() {
        AtualizarProdutoGestorRequest request = new AtualizarProdutoGestorRequest(
                "Hamburguer",
                "Artesanal",
                null,
                new BigDecimal("29.90"),
                1
        );
        ProdutoGestorClientResponse produtoAtualizado = criarProduto(1, "Hamburguer", true);

        when(produtoClient.atualizarProduto(eq(1), any(AtualizarProdutoClientRequest.class)))
                .thenReturn(produtoAtualizado);

        ProdutoGestorResponse resultado = gestorProdutoService.atualizarProduto(1, request, AUTHORIZATION);

        assertEquals("Hamburguer", resultado.nome());
        verify(produtoClient).atualizarProduto(eq(1), any(AtualizarProdutoClientRequest.class));
    }

    @Test
    void ativarProdutoDeveDelegarParaMsProdutos() {
        when(produtoClient.ativarProduto(1)).thenReturn(criarProduto(1, "Hamburguer", true));

        ProdutoGestorResponse resultado = gestorProdutoService.ativarProduto(1, AUTHORIZATION);

        assertEquals(true, resultado.disponivel());
        verify(produtoClient).ativarProduto(1);
    }

    @Test
    void desativarProdutoDeveMapearProdutoNaoEncontrado() {
        when(produtoClient.desativarProduto(1)).thenThrow(feignException(404));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> gestorProdutoService.desativarProduto(1, AUTHORIZATION)
        );

        assertEquals(ErrorEnum.PRODUTO_NAO_ENCONTRADO, exception.getErrorEnum());
    }

    @Test
    void listarProdutosDeveLancarTokenInvalidoQuandoAuthorizationNaoForBearer() {
        BaseException exception = assertThrows(
                BaseException.class,
                () -> gestorProdutoService.listarProdutos("token")
        );

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
        verifyNoInteractions(produtoClient);
    }

    private ProdutoGestorClientResponse criarProduto(Integer id, String nome, Boolean disponivel) {
        return new ProdutoGestorClientResponse(
                id,
                nome,
                "Descricao",
                null,
                new BigDecimal("29.90"),
                1,
                "Lanches",
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
