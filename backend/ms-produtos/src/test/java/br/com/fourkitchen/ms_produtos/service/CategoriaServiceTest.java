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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
        Categoria categoria = criarCategoria(1, "Lanches", true);
        CategoriaResponse response = criarResponse(categoria);

        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));
        when(categoriaResponseMapper.map(categoria)).thenReturn(response);

        List<CategoriaResponse> resultado = categoriaService.listarCategorias();

        assertEquals(List.of(response), resultado);
        verify(categoriaRepository).findAll();
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
    void atualizarCategoriaDeveSalvarDadosAtualizados() {
        AtualizarCategoriaRequest request = new AtualizarCategoriaRequest("Lanches Premium", "Artesanais", null);
        Categoria categoria = criarCategoria(1, "Lanches", true);
        Categoria categoriaSalva = criarCategoria(1, "Lanches Premium", true);
        CategoriaResponse response = criarResponse(categoriaSalva);

        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Lanches Premium", 1)).thenReturn(false);
        when(categoriaRepository.findById(1)).thenReturn(java.util.Optional.of(categoria));
        when(categoriaRepository.save(categoria)).thenReturn(categoriaSalva);
        when(categoriaResponseMapper.map(categoriaSalva)).thenReturn(response);

        CategoriaResponse resultado = categoriaService.atualizarCategoria(1, request);

        assertSame(response, resultado);
        verify(categoriaRepository).existsByNomeIgnoreCaseAndIdNot("Lanches Premium", 1);
        verify(categoriaRepository).findById(1);
        verify(atualizarCategoriaRequestMapper).map(request, categoria);
        verify(categoriaRepository).save(categoria);
        verify(categoriaResponseMapper).map(categoriaSalva);
    }

    @Test
    void atualizarCategoriaDeveBloquearNomeDuplicado() {
        AtualizarCategoriaRequest request = new AtualizarCategoriaRequest("Lanches", "Sanduiches", null);
        Categoria categoria = criarCategoria(1, "Bebidas", true);

        when(categoriaRepository.findById(1)).thenReturn(java.util.Optional.of(categoria));
        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Lanches", 1)).thenReturn(true);

        BaseException exception = assertThrows(BaseException.class, () -> categoriaService.atualizarCategoria(1, request));

        assertEquals(ErrorEnum.CATEGORIA_NOME_DUPLICADO, exception.getErrorEnum());
        verify(categoriaRepository).findById(1);
        verify(categoriaRepository).existsByNomeIgnoreCaseAndIdNot("Lanches", 1);
        verify(categoriaRepository, never()).save(any());
        verifyNoInteractions(atualizarCategoriaRequestMapper, categoriaResponseMapper);
    }

    @Test
    void ativarCategoriaDeveSalvarCategoriaAtiva() {
        Categoria categoria = criarCategoria(1, "Lanches", false);
        Categoria categoriaSalva = criarCategoria(1, "Lanches", true);
        CategoriaResponse response = criarResponse(categoriaSalva);

        when(categoriaRepository.findById(1)).thenReturn(java.util.Optional.of(categoria));
        when(categoriaRepository.save(categoria)).thenReturn(categoriaSalva);
        when(categoriaResponseMapper.map(categoriaSalva)).thenReturn(response);

        CategoriaResponse resultado = categoriaService.ativarCategoria(1);

        assertSame(response, resultado);
        assertEquals(true, categoria.getAtivo());
        verify(categoriaRepository).findById(1);
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void desativarCategoriaDeveSalvarCategoriaInativa() {
        Categoria categoria = criarCategoria(1, "Lanches", true);
        Categoria categoriaSalva = criarCategoria(1, "Lanches", false);
        CategoriaResponse response = criarResponse(categoriaSalva);

        when(categoriaRepository.findById(1)).thenReturn(java.util.Optional.of(categoria));
        when(categoriaRepository.save(categoria)).thenReturn(categoriaSalva);
        when(categoriaResponseMapper.map(categoriaSalva)).thenReturn(response);

        CategoriaResponse resultado = categoriaService.desativarCategoria(1);

        assertSame(response, resultado);
        assertEquals(false, categoria.getAtivo());
        verify(categoriaRepository).findById(1);
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void desativarCategoriaDeveLancarExcecaoQuandoCategoriaNaoExistir() {
        when(categoriaRepository.findById(99)).thenReturn(java.util.Optional.empty());

        BaseException exception = assertThrows(BaseException.class, () -> categoriaService.desativarCategoria(99));

        assertEquals(ErrorEnum.CATEGORIA_NAO_ENCONTRADA, exception.getErrorEnum());
        verify(categoriaRepository).findById(99);
        verify(categoriaRepository, never()).save(any());
        verifyNoInteractions(categoriaResponseMapper);
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
