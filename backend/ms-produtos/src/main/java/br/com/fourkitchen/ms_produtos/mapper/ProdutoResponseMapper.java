package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.ProdutoResponse;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import br.com.fourkitchen.ms_produtos.model.Produto;
import org.springframework.stereotype.Component;

@Component
public class ProdutoResponseMapper implements Mapper<Produto, ProdutoResponse> {

    private final ImagemBase64Mapper imagemBase64Mapper;

    public ProdutoResponseMapper(ImagemBase64Mapper imagemBase64Mapper) {
        this.imagemBase64Mapper = imagemBase64Mapper;
    }

    @Override
    public ProdutoResponse map(Produto source) {
        Categoria categoria = source.getCategoria();

        return new ProdutoResponse(
                source.getId(),
                source.getNome(),
                source.getDescricao(),
                imagemBase64Mapper.paraBase64(source.getImagem()),
                source.getPreco(),
                categoria != null ? categoria.getId() : null,
                categoria != null ? categoria.getNome() : null,
                source.getDisponivel()
        );
    }
}
