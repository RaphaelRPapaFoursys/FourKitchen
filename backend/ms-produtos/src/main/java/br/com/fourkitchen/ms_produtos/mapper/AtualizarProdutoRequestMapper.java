package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.request.AtualizarProdutoRequest;
import br.com.fourkitchen.ms_produtos.model.Produto;
import org.springframework.stereotype.Component;

@Component
public class AtualizarProdutoRequestMapper {

    private final ImagemBase64Mapper imagemBase64Mapper;

    public AtualizarProdutoRequestMapper(ImagemBase64Mapper imagemBase64Mapper) {
        this.imagemBase64Mapper = imagemBase64Mapper;
    }

    public void map(AtualizarProdutoRequest source, Produto target) {
        target.setNome(source.nome());
        target.setDescricao(source.descricao());
        if (source.imagem() != null) {
            target.setImagem(imagemBase64Mapper.paraBytes(source.imagem()));
        }
        target.setPreco(source.preco());
    }
}
