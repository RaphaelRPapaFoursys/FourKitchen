package br.com.fourkitchen.ms_notificacoes.dto.request;

import br.com.fourkitchen.ms_notificacoes.enums.DestinoNotificacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarNotificacaoRequest(
        @NotBlank(message = "Tipo e obrigatorio.")
        @Size(max = 50, message = "Tipo deve ter no maximo 50 caracteres.")
        String tipo,

        @NotBlank(message = "Mensagem e obrigatoria.")
        @Size(max = 255, message = "Mensagem deve ter no maximo 255 caracteres.")
        String mensagem,

        @NotNull(message = "Destino e obrigatorio.")
        DestinoNotificacao destino
) {
}
