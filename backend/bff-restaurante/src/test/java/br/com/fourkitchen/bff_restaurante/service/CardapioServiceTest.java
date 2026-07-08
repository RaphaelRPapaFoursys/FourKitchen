package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaCardapioClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoCardapioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaCardapioResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ProdutoCardapioResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.mapper.CardapioResponseMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardapioServiceTest {

    @Mock
    private ProdutoClient produtoClient;

    @Mock
    private CardapioResponseMapper cardapioResponseMapper;

    @InjectMocks
    private CardapioService cardapioService;

    @Test
    void buscarCardapioDeveRetornarCardapioDoMsProdutos() {
        CategoriaCardapioClientResponse categoriaClientResponse = new CategoriaCardapioClientResponse(
                1,
                "Lanches",
                "Sanduiches",
                "categoriaBase64",
                List.of(new ProdutoCardapioClientResponse(
                        10,
                        "X-Burger",
                        "Pao, carne e queijo",
                        "aW1hZ2Vt",
                        new BigDecimal("29.90")
                ))
        );
        CategoriaCardapioResponse categoriaResponse = new CategoriaCardapioResponse(
                1,
                "Lanches",
                "Sanduiches",
                "categoriaBase64",
                List.of(new ProdutoCardapioResponse(
                        10,
                        "X-Burger",
                        "Pao, carne e queijo",
                        "aW1hZ2Vt",
                        new BigDecimal("29.90")
                ))
        );
        List<CategoriaCardapioResponse> cardapio = List.of(categoriaResponse);

        when(produtoClient.buscarCardapio()).thenReturn(List.of(categoriaClientResponse));
        when(cardapioResponseMapper.map(categoriaClientResponse)).thenReturn(categoriaResponse);

        List<CategoriaCardapioResponse> resultado = cardapioService.buscarCardapio();

        assertEquals(cardapio, resultado);
        verify(produtoClient).buscarCardapio();
        verify(cardapioResponseMapper).map(categoriaClientResponse);
    }

    @Test
    void buscarCardapioDeveLancarServicoProdutosIndisponivelQuandoMsProdutosFalhar() {
        when(produtoClient.buscarCardapio()).thenThrow(feignException(500));

        BaseException exception = assertThrows(BaseException.class, () -> cardapioService.buscarCardapio());

        assertEquals(ErrorEnum.MS_PRODUTOS_INDISPONIVEL, exception.getErrorEnum());
        verify(produtoClient).buscarCardapio();
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/produtos/cardapio",
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

        return FeignException.errorStatus("buscarCardapio", response);
    }
}
