package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.request.AtualizarProdutoRequest;
import br.com.fourkitchen.ms_produtos.model.Produto;
import org.springframework.stereotype.Component;

@Component
public class AtualizarProdutoRequestMapper {
    public void map(AtualizarProdutoRequest source, Produto target) {
        target.setNome(source.nome());
        target.setDescricao(source.descricao());
        target.setPreco(source.preco());
    }
}
