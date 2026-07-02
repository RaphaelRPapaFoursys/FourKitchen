package br.com.fourkitchen.ms_notificacoes.dto.request;

import br.com.fourkitchen.ms_notificacoes.enums.DestinoNotificacao;
import br.com.fourkitchen.ms_notificacoes.enums.TipoNotificacao;
import jakarta.validation.constraints.NotNull;

public record CriarNotificacaoRequest(
        @NotNull(message = "Tipo e obrigatorio.")
        TipoNotificacao tipo,

        @NotNull(message = "Destino e obrigatorio.")
        DestinoNotificacao destino
) {
}
