package br.com.fourkitchen.ms_produtos.service;

import br.com.fourkitchen.ms_produtos.dto.request.AtualizarCategoriaRequest;
import br.com.fourkitchen.ms_produtos.dto.request.CriarCategoriaRequest;
import br.com.fourkitchen.ms_produtos.dto.response.CategoriaResponse;
import br.com.fourkitchen.ms_produtos.exception.BaseException;
import br.com.fourkitchen.ms_produtos.exception.ErrorEnum;
import br.com.fourkitchen.ms_produtos.mapper.AtualizarCategoriaRequestMapper;
import br.com.fourkitchen.ms_produtos.mapper.CategoriaResponseMapper;
import br.com.fourkitchen.ms_produtos.mapper.CriarCategoriaRequestMapper;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import br.com.fourkitchen.ms_produtos.repository.CategoriaRepository;
import br.com.fourkitchen.ms_produtos.repository.CategoriaGestorProjection;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private CategoriaResponseMapper categoriaResponseMapper;

    @Mock
    private CriarCategoriaRequestMapper criarCategoriaRequestMapper;

    @Mock
    private AtualizarCategoriaRequestMapper atualizarCategoriaRequestMapper;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void listarCategoriasDeveRetornarCategoriasMapeadas() {
        CategoriaGestorProjection categoria = mock(CategoriaGestorProjection.class);
        CategoriaResponse response = new CategoriaResponse(1, "Lanches", null, null, true);
        PageRequest pageable = PageRequest.of(0, 10);

        when(categoriaRepository.buscarCategoriasParaGestao(pageable))
                .thenReturn(new PageImpl<>(List.of(categoria), pageable, 1));
        when(categoriaResponseMapper.map(categoria)).thenReturn(response);

        var resultado = categoriaService.listarCategorias(null, pageable);

        assertEquals(List.of(response), resultado.content());
        assertEquals(1, resultado.totalElements());
        verify(categoriaRepository).buscarCategoriasParaGestao(pageable);
        verify(categoriaResponseMapper).map(categoria);
    }

    @Test
    void criarCategoriaDeveSalvarCategoriaAtiva() {
        CriarCategoriaRequest request = new CriarCategoriaRequest("Lanches", "Sanduiches", null);
        Categoria categoriaMapeada = criarCategoria(null, "Lanches", null);
        Categoria categoriaSalva = criarCategoria(1, "Lanches", true);
        CategoriaResponse response = criarResponse(categoriaSalva);

        when(categoriaRepository.existsByNomeIgnoreCase("Lanches")).thenReturn(false);
        when(criarCategoriaRequestMapper.map(request)).thenReturn(categoriaMapeada);
        when(categoriaRepository.save(categoriaMapeada)).thenReturn(categoriaSalva);
        when(categoriaResponseMapper.map(categoriaSalva)).thenReturn(response);

        CategoriaResponse resultado = categoriaService.criarCategoria(request);

        assertSame(response, resultado);

        ArgumentCaptor<Categoria> categoriaCaptor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(categoriaCaptor.capture());

        Categoria categoriaEnviadaParaSalvar = categoriaCaptor.getValue();
        assertEquals(true, categoriaEnviadaParaSalvar.getAtivo());
        verify(categoriaRepository).existsByNomeIgnoreCase("Lanches");
        verify(criarCategoriaRequestMapper).map(request);
        verify(categoriaResponseMapper).map(categoriaSalva);
    }

    @Test
    void criarCategoriaDeveBloquearNomeDuplicado() {
        CriarCategoriaRequest request = new CriarCategoriaRequest("Lanches", "Sanduiches", null);

        when(categoriaRepository.existsByNomeIgnoreCase("Lanches")).thenReturn(true);

        BaseException exception = assertThrows(BaseException.class, () -> categoriaService.criarCategoria(request));

        assertEquals(ErrorEnum.CATEGORIA_NOME_DUPLICADO, exception.getErrorEnum());
        verify(categoriaRepository).existsByNomeIgnoreCase("Lanches");
        verify(categoriaRepository, never()).save(any());
        verifyNoInteractions(criarCategoriaRequestMapper, categoriaResponseMapper);
    }

    @Test
    void atualizarCategoriaDeveSalvarCategoriaAtualizada() {
        AtualizarCategoriaRequest request = new AtualizarCategoriaRequest("Lanches", "Sanduiches", null);
        Categoria categoria = criarCategoria(1, "Lanches Antigo", true);
        CategoriaResponse response = criarResponse(categoria);

        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Lanches", 1)).thenReturn(false);
        when(categoriaRepository.save(categoria)).thenReturn(categoria);
        when(categoriaResponseMapper.map(categoria)).thenReturn(response);

        CategoriaResponse resultado = categoriaService.atualizarCategoria(1, request);

        assertSame(response, resultado);
        verify(atualizarCategoriaRequestMapper).map(request, categoria);
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void atualizarCategoriaDeveBloquearNomeDuplicadoDeOutraCategoria() {
        AtualizarCategoriaRequest request = new AtualizarCategoriaRequest("Lanches", "Sanduiches", null);
        Categoria categoria = criarCategoria(1, "Entradas", true);

        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Lanches", 1)).thenReturn(true);

        BaseException exception = assertThrows(BaseException.class, () -> categoriaService.atualizarCategoria(1, request));

        assertEquals(ErrorEnum.CATEGORIA_NOME_DUPLICADO, exception.getErrorEnum());
        verify(categoriaRepository, never()).save(any());
        verifyNoInteractions(atualizarCategoriaRequestMapper, categoriaResponseMapper);
    }

    @Test
    void desativarCategoriaDeveSalvarCategoriaInativa() {
        Categoria categoria = criarCategoria(1, "Lanches", true);
        CategoriaResponse response = criarResponse(categoria);

        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(categoria)).thenReturn(categoria);
        when(categoriaResponseMapper.map(categoria)).thenReturn(response);

        CategoriaResponse resultado = categoriaService.desativarCategoria(1);

        assertSame(response, resultado);
        assertEquals(false, categoria.getAtivo());
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void ativarCategoriaDeveSalvarCategoriaAtiva() {
        Categoria categoria = criarCategoria(1, "Lanches", false);
        CategoriaResponse response = criarResponse(categoria);

        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(categoria)).thenReturn(categoria);
        when(categoriaResponseMapper.map(categoria)).thenReturn(response);

        CategoriaResponse resultado = categoriaService.ativarCategoria(1);

        assertSame(response, resultado);
        assertEquals(true, categoria.getAtivo());
        verify(categoriaRepository).save(categoria);
    }

    private Categoria criarCategoria(Integer id, String nome, Boolean ativo) {
        return Categoria.builder()
                .id(id)
                .nome(nome)
                .ativo(ativo)
                .build();
    }

    private CategoriaResponse criarResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                null,
                categoria.getAtivo()
        );
    }
}
