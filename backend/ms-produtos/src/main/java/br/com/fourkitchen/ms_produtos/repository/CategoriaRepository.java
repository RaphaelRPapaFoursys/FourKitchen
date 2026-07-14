package br.com.fourkitchen.ms_produtos.repository;

import br.com.fourkitchen.ms_produtos.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    @Query("""
            select
                c.id as id,
                c.nome as nome,
                c.descricao as descricao,
                c.imagemAtualizadaEm as imagemAtualizadaEm,
                case when c.imagem is null then false else true end as possuiImagem
            from Categoria c
            where c.ativo = true
            order by c.nome
            """)
    List<CategoriaCardapioProjection> buscarCategoriasAtivasParaCardapio();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Integer id);
}
