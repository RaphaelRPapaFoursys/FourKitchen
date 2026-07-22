package br.com.fourkitchen.ms_pedidos.repository;

import br.com.fourkitchen.ms_pedidos.entities.ProdutoPedido;
import br.com.fourkitchen.ms_pedidos.repository.projection.ProdutoRankingProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


import java.util.Optional;

public interface ProdutoPedidoRepository extends JpaRepository<ProdutoPedido, Integer> {

    List<ProdutoPedido> findByIdPedidoIn(Collection<Integer> idsPedidos);
    Optional<ProdutoPedido> findByIdPedidoAndId(Integer idPedido, Integer idProdutoPedido);

    @Query(value = """
            SELECT pp.id_produto AS "idProduto",
                   COALESCE(MAX(NULLIF(TRIM(pp.nome_produto), '')), CONCAT('Produto #', pp.id_produto)) AS "nomeProduto",
                   SUM(pp.quantidade) AS quantidade
              FROM produtos_pedidos pp
              JOIN pedidos p ON p.id = pp.id_pedido
             WHERE p.data_criacao >= :inicio
               AND p.data_criacao < :fim
               AND p.status <> 'CANCELADO'
               AND pp.status_produto_pedido NOT IN ('CANCELADO', 'REMOVIDO')
             GROUP BY pp.id_produto
             ORDER BY SUM(pp.quantidade) DESC, "nomeProduto" ASC, pp.id_produto ASC
             LIMIT 5
            """, nativeQuery = true)
    List<ProdutoRankingProjection> buscarRankingProdutos(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}
