package br.com.fourkitchen.bff_restaurante.dto.response;

import br.com.fourkitchen.bff_restaurante.dto.PeriodoDashboard;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Cinco produtos mais pedidos no período.")
public record RankingProdutosResponse(
        PeriodoDashboard periodo,
        List<Item> dados
) {
    public record Item(Integer idProduto, String nomeProduto, long quantidade) {
    }
}
