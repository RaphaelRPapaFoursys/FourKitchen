package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.ProdutoCardapioResponse;

import java.util.List;

public record CategoriaCardapioMapperSource(
        Integer categoriaId,
        String categoriaNome,
        String categoriaDescricao,
        List<ProdutoCardapioResponse> produtos
) {
}
