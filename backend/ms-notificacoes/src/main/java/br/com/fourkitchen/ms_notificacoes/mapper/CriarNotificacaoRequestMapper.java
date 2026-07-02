package br.com.fourkitchen.ms_notificacoes.mapper;

import br.com.fourkitchen.ms_notificacoes.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.ms_notificacoes.model.Notificacao;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CriarNotificacaoRequestMapper implements Mapper<CriarNotificacaoRequest, Notificacao> {

    @Override
    public Notificacao map(CriarNotificacaoRequest source) {
        return Notificacao.builder()
                .tipo(source.tipo().name())
                .mensagem(source.tipo().getMensagemPadrao())
                .destino(source.destino())
                .lida(false)
                .data(LocalDateTime.now())
                .build();
    }
}
