package br.com.fourkitchen.ms_pedidos.dto.request;

import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record CriarPedidoRequest(
        Integer id,
        @NotNull(message = "O campo codigo não pode ser nulo")
        Integer codigo,
        @NotNull(message = "O campo canal não pode ser nulo")
        CanaisPedido canal,
        @NotNull(message = "O campo status não pode ser nulo")
        StatusPedido status,
        @NotNull(message = "O campo idMesa não pode ser nulo")
        Integer idMesa,
        @NotNull(message = "O campo idUsuario não pode ser nulo")
        Integer idUsuario
) {
}
