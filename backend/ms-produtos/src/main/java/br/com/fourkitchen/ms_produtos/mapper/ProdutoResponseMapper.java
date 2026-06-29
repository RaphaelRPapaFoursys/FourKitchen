package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.ProdutoResponse;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import br.com.fourkitchen.ms_produtos.model.Produto;
import org.springframework.stereotype.Component;

@Component
public class ProdutoResponseMapper implements Mapper<Produto, ProdutoResponse> {
    @Override
    public ProdutoResponse map(Produto source) {
        Categoria categoria = source.getCategoria();

        return new ProdutoResponse(
                source.getId(),
                source.getNome(),
                source.getDescricao(),
                source.getPreco(),
                categoria != null ? categoria.getId() : null,
                categoria != null ? categoria.getNome() : null,
                source.getDisponivel()
        );
    }
}
