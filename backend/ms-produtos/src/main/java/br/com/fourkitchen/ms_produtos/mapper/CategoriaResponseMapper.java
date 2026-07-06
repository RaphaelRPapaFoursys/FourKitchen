package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.CategoriaResponse;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaResponseMapper implements Mapper<Categoria, CategoriaResponse> {
    @Override
    public CategoriaResponse map(Categoria source) {
        return new CategoriaResponse(
                source.getId(),
                source.getNome(),
                source.getDescricao(),
                source.getAtivo()
        );
    }
}
