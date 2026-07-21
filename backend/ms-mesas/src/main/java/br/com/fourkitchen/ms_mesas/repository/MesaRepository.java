package br.com.fourkitchen.ms_mesas.repository;

import br.com.fourkitchen.ms_mesas.model.Mesa;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Integer> {

    @Override
    @EntityGraph(attributePaths = "atendimento")
    List<Mesa> findAll();

    boolean existsByNumero(Integer numero);

    long countByDisponivelFalse();

    @Query("select m.id as id, m.numero as numero from Mesa m order by m.numero")
    List<MesaOpcaoProjection> buscarOpcoesOrdenadas();

    @EntityGraph(attributePaths = "atendimento")
    List<Mesa> findByDisponivelFalseAndAtendimento_GarcomIdAndAtendimento_DataFechamentoIsNullOrderByNumeroAsc(
            Integer garcomId
    );
}
