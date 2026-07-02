package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Mesa atribuida ao garcom autenticado com pedidos ativos e chamadas pendentes.")
public record MesaGarcomResponse(
        @Schema(description = "Identificador da mesa", example = "1")
        Integer idMesa,

        @Schema(description = "Numero fisico/visual da mesa", example = "10")
        Integer numero,

        @Schema(description = "Status da mesa", example = "OCUPADA")
        String status,

        @Schema(description = "Atendimento aberto da mesa", example = "8")
        Integer idAtendimento,

        @Schema(description = "Codigo da sessao aberta para a mesa", example = "123456")
        Integer codigoSessao,

        @Schema(description = "Garcom responsavel pela mesa", example = "7")
        Integer idGarcom,

        @Schema(description = "Data de abertura do atendimento", example = "2026-07-02T10:00:00")
        LocalDateTime dataAbertura,

        @Schema(description = "Pedidos ativos da mesa")
        List<PedidoAtivoMesaResponse> pedidosAtivos,

        @Schema(description = "Chamadas pendentes da mesa")
        List<ChamadaPendenteMesaResponse> chamadasPendentes,

        @Schema(description = "Indica se a mesa deve aparecer destacada por possuir chamada pendente", example = "true")
        Boolean possuiChamadaPendente
) {
}
