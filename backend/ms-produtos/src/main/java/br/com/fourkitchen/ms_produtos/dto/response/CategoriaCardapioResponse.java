package br.com.fourkitchen.ms_produtos.dto.response;

import java.util.List;

public record CategoriaCardapioResponse(
        Integer categoriaId,
        String categoriaNome,
        List<ProdutoCardapioResponse> produtos
) {
}
