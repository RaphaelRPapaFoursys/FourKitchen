package br.com.fourkitchen.ms_produtos.repository;

import br.com.fourkitchen.ms_produtos.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    @Query(
            value = """
                    select
                        c.id as id,
                        c.nome as nome,
                        c.descricao as descricao,
                        c.ativo as ativo,
                        c.imagemAtualizadaEm as imagemAtualizadaEm,
                        case when c.imagem is null then false else true end as possuiImagem
                    from Categoria c
                    order by c.nome, c.id
                    """,
            countQuery = """
                    select count(c)
                    from Categoria c
                    """
    )
    Page<CategoriaGestorProjection> buscarCategoriasParaGestao(Pageable pageable);

    @Query(
            value = """
                    select
                        c.id as id,
                        c.nome as nome,
                        c.descricao as descricao,
                        c.ativo as ativo,
                        c.imagemAtualizadaEm as imagemAtualizadaEm,
                        case when c.imagem is null then false else true end as possuiImagem
                    from Categoria c
                    where lower(c.nome) like lower(concat('%', :busca, '%'))
                       or lower(coalesce(c.descricao, '')) like lower(concat('%', :busca, '%'))
                    order by c.nome, c.id
                    """,
            countQuery = """
                    select count(c)
                    from Categoria c
                    where lower(c.nome) like lower(concat('%', :busca, '%'))
                       or lower(coalesce(c.descricao, '')) like lower(concat('%', :busca, '%'))
                    """
    )
    Page<CategoriaGestorProjection> buscarCategoriasParaGestaoComBusca(
            @Param("busca") String busca,
            Pageable pageable
    );

    @Query("""
            select c.id as id, c.nome as nome, c.ativo as ativo
            from Categoria c
            order by c.nome, c.id
            """)
    List<CategoriaOpcaoProjection> buscarOpcoesParaGestao();

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
