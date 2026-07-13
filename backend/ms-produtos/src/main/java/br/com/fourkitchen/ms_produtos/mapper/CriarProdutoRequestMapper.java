package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.request.CriarProdutoRequest;
import br.com.fourkitchen.ms_produtos.model.Produto;
import org.springframework.stereotype.Component;

@Component
public class CriarProdutoRequestMapper implements Mapper<CriarProdutoRequest, Produto> {

    private final ImagemBase64Mapper imagemBase64Mapper;

    public CriarProdutoRequestMapper(ImagemBase64Mapper imagemBase64Mapper) {
        this.imagemBase64Mapper = imagemBase64Mapper;
    }

    @Override
    public Produto map(CriarProdutoRequest source) {
        return Produto.builder()
                .nome(source.nome())
                .descricao(source.descricao())
                .imagem(imagemBase64Mapper.paraBytes(source.imagem()))
                .preco(source.preco())
                .build();
    }
}
