package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.ProdutoCardapioResponse;
import br.com.fourkitchen.ms_produtos.model.Produto;
import org.springframework.stereotype.Component;

@Component
public class ProdutoCardapioResponseMapper implements Mapper<Produto, ProdutoCardapioResponse> {

    private final ImagemBase64Mapper imagemBase64Mapper;

    public ProdutoCardapioResponseMapper(ImagemBase64Mapper imagemBase64Mapper) {
        this.imagemBase64Mapper = imagemBase64Mapper;
    }

    @Override
    public ProdutoCardapioResponse map(Produto source) {
        return new ProdutoCardapioResponse(
                source.getId(),
                source.getNome(),
                source.getDescricao(),
                imagemBase64Mapper.paraBase64(source.getImagem()),
                source.getPreco()
        );
    }
}
