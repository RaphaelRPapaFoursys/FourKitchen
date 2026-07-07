package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGestorResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MesaGestorResponseMapperTest {

    private final MesaGestorResponseMapper mapper = new MesaGestorResponseMapper();

    @Test
    void mapDeveMontarMesaDoGestorComNomeDoGarcom() {
        MesaClientResponse mesa = new MesaClientResponse(
                1,
                10,
                "OCUPADA",
                7,
                123456,
                LocalDateTime.of(2026, 7, 2, 10, 0),
                null
        );

        MesaGestorResponse response = mapper.map(new MesaGestorMapperSource(mesa, "Amanda Souza"));

        assertEquals(1, response.id());
        assertEquals(10, response.numero());
        assertEquals("OCUPADA", response.status());
        assertEquals(7, response.garcomId());
        assertEquals("Amanda Souza", response.garcomNome());
        assertEquals(123456, response.codigoSessao());
        assertEquals(LocalDateTime.of(2026, 7, 2, 10, 0), response.dataAbertura());
        assertEquals(null, response.dataFechamento());
    }
}
