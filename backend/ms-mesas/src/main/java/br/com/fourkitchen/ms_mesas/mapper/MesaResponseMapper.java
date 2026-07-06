package br.com.fourkitchen.ms_mesas.mapper;

import br.com.fourkitchen.ms_mesas.dto.response.MesaResponse;
import br.com.fourkitchen.ms_mesas.enums.StatusMesa;
import br.com.fourkitchen.ms_mesas.model.Mesa;
import org.springframework.stereotype.Component;

@Component
public class MesaResponseMapper implements Mapper<Mesa, MesaResponse> {

    @Override
    public MesaResponse map(Mesa source) {
        return new MesaResponse(
                source.getId(),
                source.getNumero(),
                Boolean.TRUE.equals(source.getDisponivel()) ? StatusMesa.DISPONIVEL : StatusMesa.OCUPADA,
                source.getAtendimento() != null ? source.getAtendimento().getGarcomId() : null,
                source.getAtendimento() != null ? source.getAtendimento().getCodigoSessao() : null,
                source.getAtendimento() != null ? source.getAtendimento().getDataAbertura() : null,
                source.getAtendimento() != null ? source.getAtendimento().getDataFechamento() : null,
                source.getAtendimento() != null ? source.getAtendimento().getId() : null
        );
    }
}
