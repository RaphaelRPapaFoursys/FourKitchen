package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.request.CriarCategoriaRequest;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CriarCategoriaRequestMapper implements Mapper<CriarCategoriaRequest, Categoria> {
    @Override
    public Categoria map(CriarCategoriaRequest source) {
        return Categoria.builder()
                .nome(source.nome().trim())
                .descricao(source.descricao())
                .build();
    }
}
