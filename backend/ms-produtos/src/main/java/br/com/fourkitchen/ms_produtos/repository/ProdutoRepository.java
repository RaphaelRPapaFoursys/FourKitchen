package br.com.fourkitchen.ms_produtos.repository;

import br.com.fourkitchen.ms_produtos.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    List<Produto> findByDisponivelTrue();

    @Query("""
            select
                p.id as id,
                p.nome as nome,
                p.descricao as descricao,
                p.preco as preco,
                c.id as categoriaId,
                c.nome as categoriaNome,
                c.descricao as categoriaDescricao,
                case when p.imagem is null then false else true end as possuiImagem
            from Produto p
            join p.categoria c
            where p.disponivel = true
              and c.ativo = true
            order by c.nome, p.nome
            """)
    List<ProdutoCardapioProjection> buscarProdutosDisponiveisParaCardapio();

    @Query(
            value = """
                    select
                        p.id as id,
                        p.nome as nome,
                        p.descricao as descricao,
                        p.preco as preco,
                        c.id as categoriaId,
                        c.nome as categoriaNome,
                        c.descricao as categoriaDescricao,
                        case when p.imagem is null then false else true end as possuiImagem
                    from Produto p
                    join p.categoria c
                    where p.disponivel = true
                      and c.ativo = true
                    order by c.nome, p.nome
                    """,
            countQuery = """
                    select count(p)
                    from Produto p
                    join p.categoria c
                    where p.disponivel = true
                      and c.ativo = true
                    """
    )
    Page<ProdutoCardapioProjection> buscarProdutosDisponiveisParaCardapio(Pageable pageable);
}
