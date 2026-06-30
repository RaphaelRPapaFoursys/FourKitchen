package br.com.fourkitchen.ms_mesas.mapper;

import br.com.fourkitchen.ms_mesas.dto.request.CriarMesaRequest;
import br.com.fourkitchen.ms_mesas.model.Mesa;
import org.springframework.stereotype.Component;

@Component
public class CriarMesaRequestMapper implements Mapper<CriarMesaRequest, Mesa> {

    @Override
    public Mesa map(CriarMesaRequest source) {
        return Mesa.builder()
                .numero(source.numero())
                .build();
    }
}
