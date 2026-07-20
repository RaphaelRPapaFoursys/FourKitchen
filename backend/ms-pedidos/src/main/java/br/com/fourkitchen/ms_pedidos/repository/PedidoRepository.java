package br.com.fourkitchen.ms_pedidos.repository;

import br.com.fourkitchen.ms_pedidos.entities.Pedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.repository.projection.CanalQuantidadeProjection;
import br.com.fourkitchen.ms_pedidos.repository.projection.VolumeHorarioProjection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    Optional<Pedido> findByIdForUpdate(@Param("id") Integer id);

    boolean existsByCodigo(Integer codigo);

    boolean existsByIdAtendimentoAndStatusIn(Integer idAtendimento, Collection<StatusPedido> status);

    long countByStatus(StatusPedido status);

    List<Pedido> findByStatusIn(Collection<StatusPedido> status);

    List<Pedido> findByStatusInOrderByDataCriacaoAscIdAsc(Collection<StatusPedido> status);

    List<Pedido> findByCanalAndStatusInOrderByDataCriacaoAscIdAsc(
            CanaisPedido canal,
            Collection<StatusPedido> status
    );

    List<Pedido> findByIdAtendimentoInAndStatusInOrderByDataCriacaoAscIdAsc(
            Collection<Integer> idsAtendimento,
            Collection<StatusPedido> status
    );

    List<Pedido> findByIdAtendimentoOrderByDataCriacaoAscIdAsc(Integer idAtendimento);
  
    List<Pedido> findByIdAtendimentoAndStatusNotOrderByDataCriacaoAscIdAsc(
            Integer idAtendimento,
            StatusPedido status
    );

    @Query(value = """
            SELECT date_trunc('hour', p.data_criacao) AS horario, COUNT(*) AS quantidade
              FROM pedidos p
             WHERE p.data_criacao >= :inicio
               AND p.data_criacao < :fim
               AND (:canal IS NULL OR p.canal = :canal)
               AND (:idMesa IS NULL OR p.id_mesa = :idMesa)
               AND (:status IS NULL OR p.status = :status)
             GROUP BY date_trunc('hour', p.data_criacao)
             ORDER BY horario ASC
            """, nativeQuery = true)
    List<VolumeHorarioProjection> contarPorHorario(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("canal") String canal,
            @Param("idMesa") Integer idMesa,
            @Param("status") String status
    );

    @Query(value = """
            SELECT p.canal AS canal, COUNT(*) AS quantidade
              FROM pedidos p
             WHERE p.data_criacao >= :inicio
               AND p.data_criacao < :fim
               AND (:canal IS NULL OR p.canal = :canal)
               AND (:idMesa IS NULL OR p.id_mesa = :idMesa)
               AND (:status IS NULL OR p.status = :status)
             GROUP BY p.canal
            """, nativeQuery = true)
    List<CanalQuantidadeProjection> contarPorCanal(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("canal") String canal,
            @Param("idMesa") Integer idMesa,
            @Param("status") String status
    );

}
