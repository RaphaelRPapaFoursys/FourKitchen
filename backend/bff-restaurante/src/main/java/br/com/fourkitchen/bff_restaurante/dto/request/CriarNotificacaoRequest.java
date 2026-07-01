package br.com.fourkitchen.bff_restaurante.dto.request;

import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criar uma notificacao via BFF.")
public record CriarNotificacaoRequest(
        @Schema(description = "Tipo da notificacao", example = "PEDIDO_PRONTO")
        @NotBlank(message = "Tipo e obrigatorio.")
        @Size(max = 50, message = "Tipo deve ter no maximo 50 caracteres.")
        String tipo,

        @Schema(description = "Mensagem exibida ao usuario", example = "Pedido pronto para retirada")
        @NotBlank(message = "Mensagem e obrigatoria.")
        @Size(max = 255, message = "Mensagem deve ter no maximo 255 caracteres.")
        String mensagem,

        @Schema(description = "Perfil ou canal que recebera a notificacao", example = "COZINHA")
        @NotNull(message = "Destino e obrigatorio.")
        DestinoNotificacao destino
) {
}
