package br.com.fourkitchen.bff_restaurante.dto.request;

import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.TipoNotificacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para criar uma notificacao via BFF.")
public record CriarNotificacaoRequest(
        @Schema(
                description = "Tipo da notificacao. A mensagem correspondente e definida automaticamente pelo backend.",
                example = "PEDIDO_PRONTO",
                allowableValues = {
                        "PEDIDO_EM_PREPARO",
                        "PEDIDO_PRONTO",
                        "PEDIDO_COM_FALTA",
                        "PEDIDO_CANCELADO",
                        "CHAMADA_GARCOM",
                        "CONTA_SOLICITADA",
                        "ALTERACAO_PEDIDO_SOLICITADA"
                }
        )
        @NotNull(message = "Tipo e obrigatorio.")
        TipoNotificacao tipo,

        @Schema(
                description = "Perfil ou canal que recebera a notificacao",
                example = "COZINHA",
                allowableValues = {
                        "GARCOM",
                        "MESA",
                        "TOTEM",
                        "COZINHA",
                        "GESTOR"
                }
        )
        @NotNull(message = "Destino e obrigatorio.")
        DestinoNotificacao destino,

        @Schema(
                description = "Mesa relacionada a notificacao, quando houver. Para CHAMADA_GARCOM, informe a mesa que solicitou atendimento.",
                example = "1"
        )
        Integer idMesa,

        @Schema(
                description = "Atendimento relacionado a notificacao. Obrigatorio para CHAMADA_GARCOM, pois a tela do garcom agrupa as chamadas por atendimento.",
                example = "8"
        )
        Integer idAtendimento,

        @Schema(description = "Garcom relacionado a notificacao, quando houver", example = "7")
        Integer idGarcom
) {
}
