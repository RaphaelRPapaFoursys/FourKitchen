package br.com.fourkitchen.bff_restaurante.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Pedido do TOTEM com problema pendente de decisao do garcom.")
public record PedidoProblemaTotemGarcomResponse(
        @Schema(example = "25") Integer id,
        @Schema(example = "100025") Integer codigo,
        @Schema(example = "AGUARDANDO_DECISAO") String status,
        LocalDateTime dataCriacao,
        @Schema(description = "Garcom que assumiu o caso; nulo enquanto estiver disponivel na fila.", example = "7")
        Integer idGarcomResponsavelProblema,
        List<ItemPedidoDetalheGarcomResponse> itens
) {
}
