package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.dto.response.MesaGestorResponse;
import org.springframework.stereotype.Component;

@Component
public class MesaGestorResponseMapper implements Mapper<MesaGestorMapperSource, MesaGestorResponse> {

    @Override
    public MesaGestorResponse map(MesaGestorMapperSource source) {
        return new MesaGestorResponse(
                source.mesa().id(),
                source.mesa().numero(),
                source.mesa().status(),
                source.mesa().garcomId(),
                source.garcomNome(),
                source.mesa().codigoSessao(),
                source.mesa().dataAbertura(),
                source.mesa().dataFechamento(),
                source.pedidos()
        );
    }
}
