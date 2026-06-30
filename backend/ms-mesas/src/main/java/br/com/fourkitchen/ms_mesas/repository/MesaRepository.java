package br.com.fourkitchen.ms_mesas.repository;

import br.com.fourkitchen.ms_mesas.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Integer> {

    boolean existsByNumero(Integer numero);
}
