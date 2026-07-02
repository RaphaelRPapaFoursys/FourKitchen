package br.com.fourkitchen.bff_restaurante.mapper;

import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ChamadaPendenteMesaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoAtivoMesaResponse;
import org.springframework.stereotype.Component;

@Component
public class MesaGarcomResponseMapper implements Mapper<MesaGarcomMapperSource, MesaGarcomResponse> {

    @Override
    public MesaGarcomResponse map(MesaGarcomMapperSource source) {
        return new MesaGarcomResponse(
                source.mesa().idMesa(),
                source.mesa().numero(),
                source.mesa().status(),
                source.mesa().idAtendimento(),
                source.mesa().codigoSessao(),
                source.mesa().idGarcom(),
                source.mesa().dataAbertura(),
                source.pedidosAtivos().stream()
                        .map(this::mapearPedidoAtivo)
                        .toList(),
                source.chamadasPendentes().stream()
                        .map(this::mapearChamadaPendente)
                        .toList(),
                !source.chamadasPendentes().isEmpty()
        );
    }

    private PedidoAtivoMesaResponse mapearPedidoAtivo(PedidoResponse pedido) {
        return new PedidoAtivoMesaResponse(
                pedido.id(),
                pedido.codigo(),
                pedido.canal(),
                pedido.status(),
                pedido.idAtendimento()
        );
    }

    private ChamadaPendenteMesaResponse mapearChamadaPendente(NotificacaoResponse chamada) {
        return new ChamadaPendenteMesaResponse(
                chamada.id(),
                chamada.tipo(),
                chamada.mensagem(),
                chamada.data()
        );
    }
}
