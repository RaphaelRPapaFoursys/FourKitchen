package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.CategoriaCardapioResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoriaCardapioResponseMapper implements Mapper<CategoriaCardapioMapperSource, CategoriaCardapioResponse> {

    private final ImagemBase64Mapper imagemBase64Mapper;

    public CategoriaCardapioResponseMapper(ImagemBase64Mapper imagemBase64Mapper) {
        this.imagemBase64Mapper = imagemBase64Mapper;
    }

    @Override
    public CategoriaCardapioResponse map(CategoriaCardapioMapperSource source) {
        return new CategoriaCardapioResponse(
                source.categoria().getId(),
                source.categoria().getNome(),
                source.categoria().getDescricao(),
                imagemBase64Mapper.paraBase64(source.categoria().getImagem()),
                source.produtos()
        );
    }
}
