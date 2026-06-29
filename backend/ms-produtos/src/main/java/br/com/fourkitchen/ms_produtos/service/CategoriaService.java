package br.com.fourkitchen.ms_produtos.service;

import br.com.fourkitchen.ms_produtos.dto.request.CriarCategoriaRequest;
import br.com.fourkitchen.ms_produtos.dto.response.CategoriaResponse;
import br.com.fourkitchen.ms_produtos.exception.BaseException;
import br.com.fourkitchen.ms_produtos.exception.ErrorEnum;
import br.com.fourkitchen.ms_produtos.mapper.CategoriaResponseMapper;
import br.com.fourkitchen.ms_produtos.mapper.CriarCategoriaRequestMapper;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import br.com.fourkitchen.ms_produtos.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    private final CategoriaResponseMapper categoriaResponseMapper;

    private final CriarCategoriaRequestMapper criarCategoriaRequestMapper;

    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAll()
                .stream()
                .map(categoriaResponseMapper::map)
                .toList();
    }

    public CategoriaResponse criarCategoria(CriarCategoriaRequest request) {
        validarNomeDisponivel(request.nome());

        Categoria categoria = criarCategoriaRequestMapper.map(request);
        categoria.setAtivo(true);

        Categoria categoriaSalva = categoriaRepository.save(categoria);

        return categoriaResponseMapper.map(categoriaSalva);
    }

    private void validarNomeDisponivel(String nome) {
        if (categoriaRepository.existsByNomeIgnoreCase(nome.trim())) {
            throw new BaseException(ErrorEnum.CATEGORIA_NOME_DUPLICADO);
        }
    }
}
