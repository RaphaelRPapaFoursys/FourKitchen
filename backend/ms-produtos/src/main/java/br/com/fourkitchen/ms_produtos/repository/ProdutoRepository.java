package br.com.fourkitchen.ms_produtos.repository;

import br.com.fourkitchen.ms_produtos.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
    List<Produto> findByDisponivelTrue();
}
