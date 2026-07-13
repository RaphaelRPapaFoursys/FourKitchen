package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.ProdutoCardapioResponse;
import br.com.fourkitchen.ms_produtos.model.Categoria;

import java.util.List;

public record CategoriaCardapioMapperSource(
        Categoria categoria,
        List<ProdutoCardapioResponse> produtos
) {
}
