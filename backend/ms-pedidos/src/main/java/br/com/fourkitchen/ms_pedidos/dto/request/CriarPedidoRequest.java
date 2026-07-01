package br.com.fourkitchen.ms_pedidos.dto.request;

import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CriarPedidoRequest(
        Integer id,
        Integer codigo,
        @NotNull(message = "O campo canal nao pode ser nulo")
        CanaisPedido canal,
        @NotNull(message = "O campo status nao pode ser nulo")
        StatusPedido status,
        Integer idMesa,
        Integer idUsuario,
        Integer idAtendimento,
        List<ProdutoPedidoRequest> itens
) {

    @AssertTrue(message = "Mesa invalida para o canal do pedido")
    public boolean isMesaValidaParaCanal() {
        if (canal == null) {
            return true;
        }

        if (CanaisPedido.TOTEM.equals(canal)) {
            return idMesa == null;
        }

        return idMesa != null;
    }
}
