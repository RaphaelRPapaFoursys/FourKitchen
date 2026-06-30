package br.com.fourkitchen.ms_mesas.client;

import org.springframework.stereotype.Component;

@Component
public class PedidosAtivosClient {

    public boolean possuiPedidosAtivos(Integer atendimentoId) {
        // TODO Integrar com ms-pedidos.
        // O ms-mesas nao deve mapear entidade/repository de pedidos, porque pedidos
        // pertencem ao microsservico ms-pedidos. Quando o ms-pedidos expuser a consulta,
        // substituir este retorno por uma chamada HTTP/gateway parecida com:
        //
        // GET /api/pedidos/atendimentos/{atendimentoId}/possui-ativos
        //
        // A resposta deve indicar se existem pedidos ativos para o atendimento atual.
        // Enquanto essa integracao nao existir, retornamos false para manter o fluxo
        // de fechamento funcionando no ms-mesas sem assumir responsabilidade do ms-pedidos.
        return false;
    }
}
