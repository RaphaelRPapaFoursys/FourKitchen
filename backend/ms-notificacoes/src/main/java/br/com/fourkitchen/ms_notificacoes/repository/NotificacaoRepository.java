package br.com.fourkitchen.ms_notificacoes.repository;

import br.com.fourkitchen.ms_notificacoes.enums.DestinoNotificacao;
import br.com.fourkitchen.ms_notificacoes.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

    List<Notificacao> findByLidaFalseOrderByDataDesc();

    List<Notificacao> findByDestinoAndLidaFalseOrderByDataDesc(DestinoNotificacao destino);

    List<Notificacao> findByTipoAndDestinoAndLidaFalseAndIdAtendimentoInOrderByDataDesc(
            String tipo,
            DestinoNotificacao destino,
            Collection<Integer> idsAtendimento
    );

    long countByTipoAndDestinoAndLidaFalse(String tipo, DestinoNotificacao destino);
}
