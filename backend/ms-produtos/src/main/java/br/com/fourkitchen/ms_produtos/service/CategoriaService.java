package br.com.fourkitchen.ms_produtos.service;

import br.com.fourkitchen.ms_produtos.dto.request.AtualizarCategoriaRequest;
import br.com.fourkitchen.ms_produtos.dto.request.CriarCategoriaRequest;
import br.com.fourkitchen.ms_produtos.dto.response.CategoriaResponse;
import br.com.fourkitchen.ms_produtos.dto.response.CategoriaImagemResponse;
import br.com.fourkitchen.ms_produtos.dto.response.CategoriaGestorPaginadaResponse;
import br.com.fourkitchen.ms_produtos.dto.response.CategoriaOpcaoResponse;
import br.com.fourkitchen.ms_produtos.exception.BaseException;
import br.com.fourkitchen.ms_produtos.exception.ErrorEnum;
import br.com.fourkitchen.ms_produtos.mapper.AtualizarCategoriaRequestMapper;
import br.com.fourkitchen.ms_produtos.mapper.CategoriaResponseMapper;
import br.com.fourkitchen.ms_produtos.mapper.CriarCategoriaRequestMapper;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import br.com.fourkitchen.ms_produtos.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    private final CategoriaResponseMapper categoriaResponseMapper;

    private final CriarCategoriaRequestMapper criarCategoriaRequestMapper;

    private final AtualizarCategoriaRequestMapper atualizarCategoriaRequestMapper;

    public CategoriaGestorPaginadaResponse listarCategorias(String busca, Pageable pageable) {
        String termo = normalizarBusca(busca);
        var pagina = termo == null
                ? categoriaRepository.buscarCategoriasParaGestao(pageable)
                : categoriaRepository.buscarCategoriasParaGestaoComBusca(termo, pageable);

        return new CategoriaGestorPaginadaResponse(
                pagina.getContent().stream().map(categoriaResponseMapper::map).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isFirst(),
                pagina.isLast()
        );
    }

    public List<CategoriaOpcaoResponse> listarOpcoes() {
        return categoriaRepository.buscarOpcoesParaGestao()
                .stream()
                .map(categoria -> new CategoriaOpcaoResponse(
                        categoria.getId(),
                        categoria.getNome(),
                        categoria.getAtivo()
                ))
                .toList();
    }

    public CategoriaResponse criarCategoria(CriarCategoriaRequest request) {
        validarNomeDisponivel(request.nome());

        Categoria categoria = criarCategoriaRequestMapper.map(request);
        categoria.setAtivo(true);

        Categoria categoriaSalva = categoriaRepository.save(categoria);

        return categoriaResponseMapper.map(categoriaSalva);
    }

    public CategoriaResponse atualizarCategoria(Integer id, AtualizarCategoriaRequest request) {
        Categoria categoria = buscarPorId(id);
        validarNomeDisponivelParaAtualizacao(request.nome(), id);

        atualizarCategoriaRequestMapper.map(request, categoria);

        Categoria categoriaSalva = categoriaRepository.save(categoria);

        return categoriaResponseMapper.map(categoriaSalva);
    }

    public CategoriaResponse ativarCategoria(Integer id) {
        Categoria categoria = buscarPorId(id);
        categoria.setAtivo(true);

        Categoria categoriaSalva = categoriaRepository.save(categoria);

        return categoriaResponseMapper.map(categoriaSalva);
    }

    public CategoriaResponse desativarCategoria(Integer id) {
        Categoria categoria = buscarPorId(id);
        categoria.setAtivo(false);

        Categoria categoriaSalva = categoriaRepository.save(categoria);

        return categoriaResponseMapper.map(categoriaSalva);
    }

    public CategoriaImagemResponse buscarImagem(Integer id) {
        Categoria categoria = buscarPorId(id);
        byte[] imagem = categoria.getImagem();

        if (imagem == null || imagem.length == 0) {
            throw new BaseException(ErrorEnum.CATEGORIA_NAO_ENCONTRADA);
        }

        return new CategoriaImagemResponse(imagem, detectarContentType(imagem));
    }

    private Categoria buscarPorId(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorEnum.CATEGORIA_NAO_ENCONTRADA));
    }

    private void validarNomeDisponivel(String nome) {
        if (categoriaRepository.existsByNomeIgnoreCase(nome.trim())) {
            throw new BaseException(ErrorEnum.CATEGORIA_NOME_DUPLICADO);
        }
    }

    private String normalizarBusca(String busca) {
        if (busca == null || busca.isBlank()) {
            return null;
        }
        return busca.trim();
    }

    private void validarNomeDisponivelParaAtualizacao(String nome, Integer id) {
        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome.trim(), id)) {
            throw new BaseException(ErrorEnum.CATEGORIA_NOME_DUPLICADO);
        }
    }

    private String detectarContentType(byte[] imagem) {
        if (imagem.length >= 3
                && (imagem[0] & 0xFF) == 0xFF
                && (imagem[1] & 0xFF) == 0xD8
                && (imagem[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }

        return "image/png";
    }
}
