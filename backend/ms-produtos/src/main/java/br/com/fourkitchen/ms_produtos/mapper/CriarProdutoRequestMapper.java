package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.request.CriarProdutoRequest;
import br.com.fourkitchen.ms_produtos.model.Produto;
import org.springframework.stereotype.Component;

@Component
public class CriarProdutoRequestMapper implements Mapper<CriarProdutoRequest, Produto> {
    @Override
    public Produto map(CriarProdutoRequest source) {
        return Produto.builder()
                .nome(source.nome())
                .descricao(source.descricao())
                .preco(source.preco())
                .build();
    }
}
