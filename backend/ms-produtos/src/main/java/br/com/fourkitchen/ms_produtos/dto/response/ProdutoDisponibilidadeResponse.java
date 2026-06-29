package br.com.fourkitchen.ms_produtos.dto.response;

public record ProdutoDisponibilidadeResponse(
        Integer produtoId,
        Boolean disponivel
) {
}
