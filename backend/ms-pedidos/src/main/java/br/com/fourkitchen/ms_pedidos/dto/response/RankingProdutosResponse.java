package br.com.fourkitchen.ms_pedidos.dto.response;

import br.com.fourkitchen.ms_pedidos.enums.PeriodoDashboard;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Ranking dos produtos mais pedidos no período.")
public record RankingProdutosResponse(
        PeriodoDashboard periodo,
        List<Item> dados
) {
    public record Item(
            Integer idProduto,
            String nomeProduto,
            long quantidade
    ) {
    }
}
