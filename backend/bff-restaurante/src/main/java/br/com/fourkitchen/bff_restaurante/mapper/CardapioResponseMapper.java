package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaCardapioClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CardapioPaginadoClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoCardapioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaCardapioResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.CardapioPaginadoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ProdutoCardapioResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CardapioResponseMapper implements Mapper<CategoriaCardapioClientResponse, CategoriaCardapioResponse> {

    @Override
    public CategoriaCardapioResponse map(CategoriaCardapioClientResponse source) {
        return new CategoriaCardapioResponse(
                source.categoriaId(),
                source.categoriaNome(),
                source.categoriaDescricao(),
                mapearProdutos(source.produtos())
        );
    }

    public CardapioPaginadoResponse map(CardapioPaginadoClientResponse source) {
        return new CardapioPaginadoResponse(
                source.content() == null ? List.of() : source.content().stream().map(this::map).toList(),
                source.page(),
                source.size(),
                source.totalElements(),
                source.totalPages(),
                source.first(),
                source.last()
        );
    }

    private List<ProdutoCardapioResponse> mapearProdutos(List<ProdutoCardapioClientResponse> produtos) {
        if (produtos == null) {
            return List.of();
        }

        return produtos.stream()
                .map(this::mapearProduto)
                .toList();
    }

    private ProdutoCardapioResponse mapearProduto(ProdutoCardapioClientResponse produto) {
        return new ProdutoCardapioResponse(
                produto.id(),
                produto.nome(),
                produto.descricao(),
                produto.imagemUrl(),
                produto.preco()
        );
    }
}
