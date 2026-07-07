package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.CategoriaCardapioResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoriaCardapioResponseMapper implements Mapper<CategoriaCardapioMapperSource, CategoriaCardapioResponse> {

    @Override
    public CategoriaCardapioResponse map(CategoriaCardapioMapperSource source) {
        return new CategoriaCardapioResponse(
                source.categoria().getId(),
                source.categoria().getNome(),
                source.produtos()
        );
    }
}
