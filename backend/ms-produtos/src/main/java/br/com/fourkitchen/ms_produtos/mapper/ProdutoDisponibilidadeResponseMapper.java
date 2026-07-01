package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.ms_produtos.model.Produto;
import org.springframework.stereotype.Component;

@Component
public class ProdutoDisponibilidadeResponseMapper implements Mapper<Produto, ProdutoDisponibilidadeResponse> {
    @Override
    public ProdutoDisponibilidadeResponse map(Produto source) {
        return new ProdutoDisponibilidadeResponse(
                source.getId(),
                Boolean.TRUE.equals(source.getDisponivel()),
                source.getPreco()
        );
    }
}
