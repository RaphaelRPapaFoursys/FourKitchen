package br.com.fourkitchen.ms_produtos.controller;

import br.com.fourkitchen.ms_produtos.dto.request.AtualizarCategoriaRequest;
import br.com.fourkitchen.ms_produtos.dto.request.CriarCategoriaRequest;
import br.com.fourkitchen.ms_produtos.dto.response.CategoriaResponse;
import br.com.fourkitchen.ms_produtos.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CategoriaController categoriaController;

    @Test
    void listarCategoriasDeveRetornarOk() {
        CategoriaResponse categoria = criarResponse(true);

        when(categoriaService.listarCategorias()).thenReturn(List.of(categoria));

        ResponseEntity<List<CategoriaResponse>> response = categoriaController.listarCategorias();

        assertEquals(200, response.getStatusCode().value());
        assertSame(categoria, response.getBody().getFirst());
        verify(categoriaService).listarCategorias();
    }

    @Test
    void criarCategoriaDeveRetornarCreated() {
        CriarCategoriaRequest request = new CriarCategoriaRequest("Lanches", "Sanduiches", null);
        CategoriaResponse categoria = criarResponse(true);

        when(categoriaService.criarCategoria(request)).thenReturn(categoria);

        ResponseEntity<CategoriaResponse> response = categoriaController.criarCategoria(request);

        assertEquals(201, response.getStatusCode().value());
        assertSame(categoria, response.getBody());
        verify(categoriaService).criarCategoria(request);
    }

    @Test
    void atualizarCategoriaDeveRetornarOk() {
        AtualizarCategoriaRequest request = new AtualizarCategoriaRequest("Lanches", "Sanduiches", null);
        CategoriaResponse categoria = criarResponse(true);

        when(categoriaService.atualizarCategoria(1, request)).thenReturn(categoria);

        ResponseEntity<CategoriaResponse> response = categoriaController.atualizarCategoria(1, request);

        assertEquals(200, response.getStatusCode().value());
        assertSame(categoria, response.getBody());
        verify(categoriaService).atualizarCategoria(1, request);
    }

    @Test
    void ativarCategoriaDeveRetornarOk() {
        CategoriaResponse categoria = criarResponse(true);

        when(categoriaService.ativarCategoria(1)).thenReturn(categoria);

        ResponseEntity<CategoriaResponse> response = categoriaController.ativarCategoria(1);

        assertEquals(200, response.getStatusCode().value());
        assertSame(categoria, response.getBody());
        verify(categoriaService).ativarCategoria(1);
    }

    @Test
    void desativarCategoriaDeveRetornarOk() {
        CategoriaResponse categoria = criarResponse(false);

        when(categoriaService.desativarCategoria(1)).thenReturn(categoria);

        ResponseEntity<CategoriaResponse> response = categoriaController.desativarCategoria(1);

        assertEquals(200, response.getStatusCode().value());
        assertSame(categoria, response.getBody());
        verify(categoriaService).desativarCategoria(1);
    }

    private CategoriaResponse criarResponse(Boolean ativo) {
        return new CategoriaResponse(1, "Lanches", "Sanduiches", null, ativo);
    }
}
