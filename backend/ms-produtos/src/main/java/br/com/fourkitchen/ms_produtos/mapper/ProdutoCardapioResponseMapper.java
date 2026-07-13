package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.ProdutoCardapioResponse;
import br.com.fourkitchen.ms_produtos.repository.ProdutoCardapioProjection;
import org.springframework.stereotype.Component;

@Component
public class ProdutoCardapioResponseMapper implements Mapper<ProdutoCardapioProjection, ProdutoCardapioResponse> {

    @Override
    public ProdutoCardapioResponse map(ProdutoCardapioProjection source) {
        return new ProdutoCardapioResponse(
                source.getId(),
                source.getNome(),
                source.getDescricao(),
                Boolean.TRUE.equals(source.getPossuiImagem()) ? "/api/produtos/" + source.getId() + "/imagem" : null,
                source.getPreco()
        );
    }
}
