package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.GarcomResumoResponse;
import org.springframework.stereotype.Component;

@Component
public class GarcomResumoResponseMapper implements Mapper<UsuarioClientResponse, GarcomResumoResponse> {

    @Override
    public GarcomResumoResponse map(UsuarioClientResponse source) {
        return new GarcomResumoResponse(
                source.id(),
                source.nome(),
                source.email()
        );
    }
}
