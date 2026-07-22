package br.com.fourkitchen.ms_pedidos.repository.projection;

public interface ProdutoRankingProjection {
    Integer getIdProduto();
    String getNomeProduto();
    Long getQuantidade();
}
