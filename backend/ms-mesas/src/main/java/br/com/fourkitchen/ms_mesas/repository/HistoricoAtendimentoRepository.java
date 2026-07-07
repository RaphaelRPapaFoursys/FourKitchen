package br.com.fourkitchen.ms_mesas.repository;

import br.com.fourkitchen.ms_mesas.model.HistoricoAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoAtendimentoRepository extends JpaRepository<HistoricoAtendimento, Integer> {

    List<HistoricoAtendimento> findAllByOrderByDataFechamentoDescIdDesc();
}
