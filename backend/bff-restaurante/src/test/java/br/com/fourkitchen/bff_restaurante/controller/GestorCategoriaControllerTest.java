package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarCategoriaGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarCategoriaGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaGestorResponse;
import br.com.fourkitchen.bff_restaurante.service.GestorCategoriaService;
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
class GestorCategoriaControllerTest {

    private static final String AUTHORIZATION = "Bearer token";

    @Mock
    private GestorCategoriaService gestorCategoriaService;

    @InjectMocks
    private GestorCategoriaController gestorCategoriaController;

    @Test
    void listarCategoriasDeveRetornarOk() {
        CategoriaGestorResponse categoria = criarCategoria(true);

        when(gestorCategoriaService.listarCategorias(AUTHORIZATION)).thenReturn(List.of(categoria));

        ResponseEntity<List<CategoriaGestorResponse>> response = gestorCategoriaController.listarCategorias(AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(categoria, response.getBody().getFirst());
        verify(gestorCategoriaService).listarCategorias(AUTHORIZATION);
    }

    @Test
    void criarCategoriaDeveRetornarCreated() {
        CriarCategoriaGestorRequest request = new CriarCategoriaGestorRequest("Lanches", "Sanduiches", null);
        CategoriaGestorResponse categoria = criarCategoria(true);

        when(gestorCategoriaService.criarCategoria(request, AUTHORIZATION)).thenReturn(categoria);

        ResponseEntity<CategoriaGestorResponse> response = gestorCategoriaController.criarCategoria(
                request,
                AUTHORIZATION
        );

        assertEquals(201, response.getStatusCode().value());
        assertSame(categoria, response.getBody());
        verify(gestorCategoriaService).criarCategoria(request, AUTHORIZATION);
    }

    @Test
    void atualizarCategoriaDeveRetornarOk() {
        AtualizarCategoriaGestorRequest request = new AtualizarCategoriaGestorRequest(
                "Lanches",
                "Sanduiches",
                null
        );
        CategoriaGestorResponse categoria = criarCategoria(true);

        when(gestorCategoriaService.atualizarCategoria(1, request, AUTHORIZATION)).thenReturn(categoria);

        ResponseEntity<CategoriaGestorResponse> response = gestorCategoriaController.atualizarCategoria(
                1,
                request,
                AUTHORIZATION
        );

        assertEquals(200, response.getStatusCode().value());
        assertSame(categoria, response.getBody());
        verify(gestorCategoriaService).atualizarCategoria(1, request, AUTHORIZATION);
    }

    @Test
    void ativarCategoriaDeveRetornarOk() {
        CategoriaGestorResponse categoria = criarCategoria(true);

        when(gestorCategoriaService.ativarCategoria(1, AUTHORIZATION)).thenReturn(categoria);

        ResponseEntity<CategoriaGestorResponse> response = gestorCategoriaController.ativarCategoria(1, AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(categoria, response.getBody());
        verify(gestorCategoriaService).ativarCategoria(1, AUTHORIZATION);
    }

    @Test
    void desativarCategoriaDeveRetornarOk() {
        CategoriaGestorResponse categoria = criarCategoria(false);

        when(gestorCategoriaService.desativarCategoria(1, AUTHORIZATION)).thenReturn(categoria);

        ResponseEntity<CategoriaGestorResponse> response = gestorCategoriaController.desativarCategoria(1, AUTHORIZATION);

        assertEquals(200, response.getStatusCode().value());
        assertSame(categoria, response.getBody());
        verify(gestorCategoriaService).desativarCategoria(1, AUTHORIZATION);
    }

    private CategoriaGestorResponse criarCategoria(Boolean ativo) {
        return new CategoriaGestorResponse(1, "Lanches", "Sanduiches", null, ativo);
    }
}
