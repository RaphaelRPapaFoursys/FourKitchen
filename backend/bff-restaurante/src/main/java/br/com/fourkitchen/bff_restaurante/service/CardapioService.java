package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaCardapioResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.mapper.CardapioResponseMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
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
}
