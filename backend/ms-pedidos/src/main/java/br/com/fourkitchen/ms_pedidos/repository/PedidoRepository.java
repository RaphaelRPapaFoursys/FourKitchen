package br.com.fourkitchen.ms_pedidos.repository;

import br.com.fourkitchen.ms_pedidos.entities.Pedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    boolean existsByCodigo(Integer codigo);

    boolean existsByIdAtendimentoAndStatusIn(Integer idAtendimento, Collection<StatusPedido> status);

    long countByStatus(StatusPedido status);

    List<Pedido> findByStatusIn(Collection<StatusPedido> status);

    List<Pedido> findByStatusInOrderByDataCriacaoAscIdAsc(Collection<StatusPedido> status);

    List<Pedido> findByIdAtendimentoInAndStatusInOrderByDataCriacaoAscIdAsc(
            Collection<Integer> idsAtendimento,
            Collection<StatusPedido> status
    );
}
