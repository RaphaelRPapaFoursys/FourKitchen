package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.CriarCategoriaRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarProdutoRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaGestorResponse;
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

    @Mock
    private GestorProdutoService gestorProdutoService;

    @InjectMocks
    private GestorProdutoController gestorProdutoController;

    @Test
    void listarProdutosDeveRetornarOk() {
        ProdutoGestorResponse produto = criarProduto();

        when(gestorProdutoService.listarProdutos()).thenReturn(List.of(produto));

        ResponseEntity<List<ProdutoGestorResponse>> response = gestorProdutoController.listarProdutos();

        assertEquals(200, response.getStatusCode().value());
        assertSame(produto, response.getBody().getFirst());
        verify(gestorProdutoService).listarProdutos();
    }

    @Test
    void criarProdutoDeveRetornarCreated() {
        CriarProdutoRequest request = new CriarProdutoRequest("Pudim", null, null, BigDecimal.TEN, 1, true);
        ProdutoGestorResponse produto = criarProduto();

        when(gestorProdutoService.criarProduto(request)).thenReturn(produto);

        ResponseEntity<ProdutoGestorResponse> response = gestorProdutoController.criarProduto(request);

        assertEquals(201, response.getStatusCode().value());
        assertSame(produto, response.getBody());
    }

    @Test
    void desativarProdutoDeveRetornarOk() {
        ProdutoGestorResponse produto = criarProduto();

        when(gestorProdutoService.desativarProduto(1)).thenReturn(produto);

        ResponseEntity<ProdutoGestorResponse> response = gestorProdutoController.desativarProduto(1);

        assertEquals(200, response.getStatusCode().value());
        assertSame(produto, response.getBody());
    }

    @Test
    void listarCategoriasDeveRetornarOk() {
        CategoriaGestorResponse categoria = new CategoriaGestorResponse(1, "Bebidas", null, true);

        when(gestorProdutoService.listarCategorias()).thenReturn(List.of(categoria));

        ResponseEntity<List<CategoriaGestorResponse>> response = gestorProdutoController.listarCategorias();

        assertEquals(200, response.getStatusCode().value());
        assertSame(categoria, response.getBody().getFirst());
    }

    @Test
    void criarCategoriaDeveRetornarCreated() {
        CriarCategoriaRequest request = new CriarCategoriaRequest("Bebidas", null);
        CategoriaGestorResponse categoria = new CategoriaGestorResponse(1, "Bebidas", null, true);

        when(gestorProdutoService.criarCategoria(request)).thenReturn(categoria);

        ResponseEntity<CategoriaGestorResponse> response = gestorProdutoController.criarCategoria(request);

        assertEquals(201, response.getStatusCode().value());
        assertSame(categoria, response.getBody());
    }

    private ProdutoGestorResponse criarProduto() {
        return new ProdutoGestorResponse(
                1,
                "Pudim",
                null,
                null,
                BigDecimal.TEN,
                1,
                "Sobremesas",
                true
        );
    }
}
