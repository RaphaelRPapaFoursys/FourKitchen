package br.com.fourkitchen.ms_mesas.repository;

import br.com.fourkitchen.ms_mesas.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Integer> {

    boolean existsByNumero(Integer numero);

    long countByDisponivelFalse();

    List<Mesa> findByDisponivelFalseAndAtendimento_GarcomIdAndAtendimento_DataFechamentoIsNullOrderByNumeroAsc(
            Integer garcomId
    );
}
