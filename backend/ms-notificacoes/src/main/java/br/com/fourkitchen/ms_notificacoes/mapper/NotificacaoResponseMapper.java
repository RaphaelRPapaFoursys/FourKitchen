package br.com.fourkitchen.ms_notificacoes.mapper;

import br.com.fourkitchen.ms_notificacoes.dto.response.NotificacaoResponse;
import br.com.fourkitchen.ms_notificacoes.model.Notificacao;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoResponseMapper implements Mapper<Notificacao, NotificacaoResponse> {

    @Override
    public NotificacaoResponse map(Notificacao notificacao) {
        return new NotificacaoResponse(
                notificacao.getId(),
                notificacao.getTipo(),
                notificacao.getMensagem(),
                notificacao.getDestino(),
                notificacao.getLida(),
                notificacao.getData()
        );
    }
}
