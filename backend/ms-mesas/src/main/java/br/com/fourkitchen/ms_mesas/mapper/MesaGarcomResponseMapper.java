package br.com.fourkitchen.ms_mesas.mapper;

import br.com.fourkitchen.ms_mesas.dto.response.MesaGarcomResponse;
import br.com.fourkitchen.ms_mesas.enums.StatusMesa;
import br.com.fourkitchen.ms_mesas.model.Atendimento;
import br.com.fourkitchen.ms_mesas.model.Mesa;
import org.springframework.stereotype.Component;

@Component
public class MesaGarcomResponseMapper implements Mapper<Mesa, MesaGarcomResponse> {

    @Override
    public MesaGarcomResponse map(Mesa source) {
        Atendimento atendimento = source.getAtendimento();

        return new MesaGarcomResponse(
                source.getId(),
                source.getNumero(),
                Boolean.TRUE.equals(source.getDisponivel()) ? StatusMesa.DISPONIVEL : StatusMesa.OCUPADA,
                atendimento != null ? atendimento.getId() : null,
                atendimento != null ? atendimento.getCodigoSessao() : null,
                atendimento != null ? atendimento.getGarcomId() : null,
                atendimento != null ? atendimento.getDataAbertura() : null
        );
    }
}
