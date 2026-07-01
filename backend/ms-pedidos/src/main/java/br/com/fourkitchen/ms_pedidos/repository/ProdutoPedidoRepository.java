package br.com.fourkitchen.ms_pedidos.repository;

import br.com.fourkitchen.ms_pedidos.entities.ProdutoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoPedidoRepository extends JpaRepository<ProdutoPedido, Integer> {
}
