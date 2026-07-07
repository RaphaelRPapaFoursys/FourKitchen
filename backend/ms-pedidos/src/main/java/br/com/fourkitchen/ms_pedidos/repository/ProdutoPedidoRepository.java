package br.com.fourkitchen.ms_pedidos.repository;

import br.com.fourkitchen.ms_pedidos.entities.ProdutoPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusProdutoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;


import java.util.Optional;

public interface ProdutoPedidoRepository extends JpaRepository<ProdutoPedido, Integer> {

    List<ProdutoPedido> findByIdPedidoAndStatus(Integer id, StatusProdutoPedido status);
    List<ProdutoPedido> findByIdPedidoIn(Collection<Integer> idsPedidos);
    Optional<ProdutoPedido> findByIdPedidoAndId(Integer idPedido, Integer idProdutoPedido);
}
