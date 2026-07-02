package br.com.fourkitchen.ms_pedidos.repository;

import br.com.fourkitchen.ms_pedidos.entities.ProdutoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ProdutoPedidoRepository extends JpaRepository<ProdutoPedido, Integer> {

    List<ProdutoPedido> findByIdPedidoIn(Collection<Integer> idsPedidos);
}
