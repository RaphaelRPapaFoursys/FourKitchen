package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.ResumoMesasOperacaoResponse;
import br.com.fourkitchen.bff_restaurante.client.notificacoes.NotificacaoClient;
import br.com.fourkitchen.bff_restaurante.client.notificacoes.dto.ResumoNotificacoesOperacaoResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ResumoPedidosOperacaoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ResumoOperacaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GestorResumoService {

    private final PedidoClient pedidoClient;

    private final MesaClient mesaClient;

    private final NotificacaoClient notificacaoClient;

    public ResumoOperacaoResponse buscarResumo() {
        ResumoPedidosOperacaoResponse pedidos = buscarResumoPedidos();
        ResumoMesasOperacaoResponse mesas = buscarResumoMesas();
        ResumoNotificacoesOperacaoResponse notificacoes = buscarResumoNotificacoes();

        return new ResumoOperacaoResponse(
                pedidos.pedidosEmPreparo(),
                pedidos.pedidosProntos(),
                mesas.mesasOcupadas(),
                pedidos.problemasPendentes(),
                notificacoes.chamadasPendentes()
        );
    }

    private ResumoPedidosOperacaoResponse buscarResumoPedidos() {
        try {
            return pedidoClient.buscarResumoOperacao();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private ResumoMesasOperacaoResponse buscarResumoMesas() {
        try {
            return mesaClient.buscarResumoOperacao();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private ResumoNotificacoesOperacaoResponse buscarResumoNotificacoes() {
        try {
            return notificacaoClient.buscarResumoOperacao();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_NOTIFICACOES_INDISPONIVEL);
        }
    }
}
