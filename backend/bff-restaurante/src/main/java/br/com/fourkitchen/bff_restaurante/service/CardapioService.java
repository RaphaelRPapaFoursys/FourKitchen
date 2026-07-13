package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaCardapioResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaCardapioResumoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.CardapioPaginadoResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.mapper.CardapioResponseMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardapioService {

    private final ProdutoClient produtoClient;

    private final CardapioResponseMapper cardapioResponseMapper;

    public List<CategoriaCardapioResponse> buscarCardapio() {
        try {
            return produtoClient.buscarCardapio()
                    .stream()
                    .map(cardapioResponseMapper::map)
                    .toList();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PRODUTOS_INDISPONIVEL);
        }
    }

    public List<CategoriaCardapioResumoResponse> buscarCategoriasAtivas() {
        try {
            return produtoClient.buscarCategoriasAtivasCardapio()
                    .stream()
                    .map(categoria -> new CategoriaCardapioResumoResponse(
                            categoria.categoriaId(),
                            categoria.categoriaNome(),
                            categoria.categoriaDescricao(),
                            categoria.imagem()
                    ))
                    .toList();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PRODUTOS_INDISPONIVEL);
        }
    }

    public CardapioPaginadoResponse buscarCardapioPaginado(Integer page, Integer size, Integer categoriaId) {
        try {
            int pagina = page == null ? 0 : Math.max(0, page);
            int tamanho = size == null ? 12 : Math.min(Math.max(1, size), 30);
            return cardapioResponseMapper.map(produtoClient.buscarCardapioPaginado(pagina, tamanho, categoriaId));
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PRODUTOS_INDISPONIVEL);
        }
    }

    public ResponseEntity<byte[]> buscarImagemProduto(Integer id) {
        try {
            return produtoClient.buscarImagem(id);
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PRODUTOS_INDISPONIVEL);
        }
    }
}
