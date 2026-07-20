package br.com.fourkitchen.ms_pedidos.repository;

import br.com.fourkitchen.ms_pedidos.entities.ProblemaCozinha;
import br.com.fourkitchen.ms_pedidos.repository.projection.MotivoQuantidadeProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProblemaCozinhaRepository extends JpaRepository<ProblemaCozinha, Integer> {

    Optional<ProblemaCozinha> findFirstByIdPedidoAndIdProdutoPedidoAndDataResolucaoIsNullOrderByDataCriacaoDescIdDesc(
            Integer idPedido,
            Integer idProdutoPedido
    );

    @Query(value = """
            SELECT pc.motivo AS motivo, COUNT(*) AS quantidade
              FROM problemas_cozinha pc
              JOIN pedidos p ON p.id = pc.id_pedido
             WHERE pc.data_criacao >= :inicio
               AND pc.data_criacao < :fim
               AND (:canal IS NULL OR p.canal = :canal)
               AND (:idMesa IS NULL OR p.id_mesa = :idMesa)
               AND (:status IS NULL OR p.status = :status)
             GROUP BY pc.motivo
             ORDER BY COUNT(*) DESC, pc.motivo ASC
            """, nativeQuery = true)
    List<MotivoQuantidadeProjection> contarPorMotivo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("canal") String canal,
            @Param("idMesa") Integer idMesa,
            @Param("status") String status
    );
}
