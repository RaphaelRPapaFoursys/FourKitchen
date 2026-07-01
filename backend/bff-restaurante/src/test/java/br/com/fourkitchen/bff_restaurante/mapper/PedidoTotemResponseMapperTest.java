package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoTotemResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PedidoTotemResponseMapperTest {

    private final PedidoTotemResponseMapper mapper = new PedidoTotemResponseMapper();

    @Test
    void mapDeveMapearPedidoResponseParaPedidoTotemResponse() {
        PedidoResponse pedido = new PedidoResponse(
                25,
                100025,
                "TOTEM",
                "ENVIADO_COZINHA",
                null,
                null,
                null
        );

        PedidoTotemResponse response = mapper.map(pedido);

        assertEquals(25, response.id());
        assertEquals(100025, response.codigo());
        assertEquals("TOTEM", response.canal());
        assertEquals("ENVIADO_COZINHA", response.status());
    }
}
