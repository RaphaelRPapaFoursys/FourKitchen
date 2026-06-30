package br.com.fourkitchen.ms_mesas.repository;

import br.com.fourkitchen.ms_mesas.model.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtendimentoRepository extends JpaRepository<Atendimento, Integer> {

    boolean existsByCodigoSessao(Integer codigoSessao);
}
