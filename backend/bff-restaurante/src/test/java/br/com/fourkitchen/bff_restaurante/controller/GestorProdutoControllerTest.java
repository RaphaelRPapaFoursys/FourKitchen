package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarProdutoGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarProdutoGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.ProdutoGestorResponse;
import br.com.fourkitchen.bff_restaurante.service.GestorProdutoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorProdutoControllerTest {

    private static final String AUTHORIZATION = "Bearer token";

    @Mock
    private GestorProdutoService gestorProdutoService;

    @InjectMocks
    private GestorProdutoController gestorProdutoController;

    @Test
    void listarProdutosDeveRetornarOk() {
        ProdutoGestorResponse produto = criarProduto(true);

        when(gestorProdutoService.listarProdutos(AUTHORIZATION)).thenReturn(List.of(produto));

        ResponseEntity<List<ProdutoGestorResponse>> response = gestorProdutoController.listarProdutos(AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(produto, response.getBody().getFirst());
        verify(gestorProdutoService).listarProdutos(AUTHORIZATION);
    }

    @Test
    void criarProdutoDeveRetornarCreated() {
        CriarProdutoGestorRequest request = new CriarProdutoGestorRequest(
                "X-Burger",
                "Pao e carne",
                null,
                new BigDecimal("29.90"),
                1
        );
        ProdutoGestorResponse produto = criarProduto(true);

        when(gestorProdutoService.criarProduto(request, AUTHORIZATION)).thenReturn(produto);

        ResponseEntity<ProdutoGestorResponse> response = gestorProdutoController.criarProduto(request, AUTHORIZATION);

        assertEquals(201, response.getStatusCode().value());
        assertSame(produto, response.getBody());
        verify(gestorProdutoService).criarProduto(request, AUTHORIZATION);
    }

    @Test
    void atualizarProdutoDeveRetornarOk() {
        AtualizarProdutoGestorRequest request = new AtualizarProdutoGestorRequest(
                "X-Burger",
                "Pao e carne",
                null,
                new BigDecimal("29.90"),
                1
        );
        ProdutoGestorResponse produto = criarProduto(true);

        when(gestorProdutoService.atualizarProduto(10, request, AUTHORIZATION)).thenReturn(produto);

        ResponseEntity<ProdutoGestorResponse> response = gestorProdutoController.atualizarProduto(
                10,
                request,
                AUTHORIZATION
        );

        assertEquals(200, response.getStatusCode().value());
        assertSame(produto, response.getBody());
        verify(gestorProdutoService).atualizarProduto(10, request, AUTHORIZATION);
    }

    @Test
    void ativarProdutoDeveRetornarOk() {
        ProdutoGestorResponse produto = criarProduto(true);

        when(gestorProdutoService.ativarProduto(10, AUTHORIZATION)).thenReturn(produto);

        ResponseEntity<ProdutoGestorResponse> response = gestorProdutoController.ativarProduto(10, AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(produto, response.getBody());
        verify(gestorProdutoService).ativarProduto(10, AUTHORIZATION);
    }

    @Test
    void desativarProdutoDeveRetornarOk() {
        ProdutoGestorResponse produto = criarProduto(false);

        when(gestorProdutoService.desativarProduto(10, AUTHORIZATION)).thenReturn(produto);

        ResponseEntity<ProdutoGestorResponse> response = gestorProdutoController.desativarProduto(10, AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(produto, response.getBody());
        verify(gestorProdutoService).desativarProduto(10, AUTHORIZATION);
    }

    private ProdutoGestorResponse criarProduto(Boolean disponivel) {
        return new ProdutoGestorResponse(
                10,
                "X-Burger",
                "Pao e carne",
                null,
                new BigDecimal("29.90"),
                1,
                "Lanches",
                disponivel
        );
    }
}
