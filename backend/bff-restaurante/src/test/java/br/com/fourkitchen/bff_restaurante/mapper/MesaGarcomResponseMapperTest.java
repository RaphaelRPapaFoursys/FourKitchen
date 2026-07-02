package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaGarcomClientResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MesaGarcomResponseMapperTest {

    private final MesaGarcomResponseMapper mapper = new MesaGarcomResponseMapper();

    @Test
    void mapDeveMontarMesaDoGarcomComPedidosAtivosEChamadasPendentes() {
        MesaGarcomClientResponse mesa = new MesaGarcomClientResponse(
                1,
                10,
                "OCUPADA",
                8,
                123456,
                7,
                LocalDateTime.of(2026, 7, 2, 10, 0)
        );
        PedidoResponse pedido = new PedidoResponse(25, 100025, "GARCOM", "ENVIADO_COZINHA", 1, 7, 8);
        NotificacaoResponse chamada = new NotificacaoResponse(
                3,
                "CHAMADA_GARCOM",
                "Cliente solicitou atendimento",
                DestinoNotificacao.GARCOM,
                false,
                LocalDateTime.of(2026, 7, 2, 10, 15),
                1,
                8,
                7
        );

        MesaGarcomResponse response = mapper.map(new MesaGarcomMapperSource(
                mesa,
                List.of(pedido),
                List.of(chamada)
        ));

        assertEquals(1, response.idMesa());
        assertEquals(10, response.numero());
        assertEquals("OCUPADA", response.status());
        assertEquals(8, response.idAtendimento());
        assertEquals(123456, response.codigoSessao());
        assertEquals(7, response.idGarcom());
        assertEquals(1, response.pedidosAtivos().size());
        assertEquals(25, response.pedidosAtivos().getFirst().id());
        assertEquals(1, response.chamadasPendentes().size());
        assertEquals(3, response.chamadasPendentes().getFirst().id());
        assertEquals(true, response.possuiChamadaPendente());
    }
}
