package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.GarcomResumoResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GarcomResumoResponseMapperTest {

    private final GarcomResumoResponseMapper mapper = new GarcomResumoResponseMapper();

    @Test
    void mapDeveMontarResumoDoGarcom() {
        UsuarioClientResponse usuario = new UsuarioClientResponse(
                7,
                "Amanda Souza",
                "amanda@fourkitchen.com",
                "GARCOM",
                true
        );

        GarcomResumoResponse response = mapper.map(usuario);

        assertEquals(7, response.id());
        assertEquals("Amanda Souza", response.nome());
        assertEquals("amanda@fourkitchen.com", response.email());
    }
}
