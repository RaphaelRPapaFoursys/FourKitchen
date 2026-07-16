package br.com.fourkitchen.ms_pedidos.repository;

import br.com.fourkitchen.ms_pedidos.entities.Pedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> findByIdForUpdate(@Param("id") Integer id);
}
