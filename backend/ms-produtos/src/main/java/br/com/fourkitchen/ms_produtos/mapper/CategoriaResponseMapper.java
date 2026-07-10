package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.CategoriaResponse;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaResponseMapper implements Mapper<Categoria, CategoriaResponse> {

    private final ImagemBase64Mapper imagemBase64Mapper;

    public CategoriaResponseMapper(ImagemBase64Mapper imagemBase64Mapper) {
        this.imagemBase64Mapper = imagemBase64Mapper;
    }

    @Override
    public CategoriaResponse map(Categoria source) {
        return new CategoriaResponse(
                source.getId(),
                source.getNome(),
                source.getDescricao(),
                imagemBase64Mapper.paraBase64(source.getImagem()),
                source.getAtivo()
        );
    }
}
