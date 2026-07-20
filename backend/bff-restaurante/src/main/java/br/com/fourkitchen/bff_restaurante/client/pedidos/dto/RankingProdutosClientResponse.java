package br.com.fourkitchen.bff_restaurante.client.pedidos.dto;

import br.com.fourkitchen.bff_restaurante.dto.PeriodoDashboard;

import java.util.List;

public record RankingProdutosClientResponse(
        PeriodoDashboard periodo,
        List<Item> dados
) {
    public record Item(Integer idProduto, String nomeProduto, long quantidade) {
    }
}
