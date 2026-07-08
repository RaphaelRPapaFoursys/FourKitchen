package br.com.fourkitchen.ms_mesas.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PedidosAtivosClient {

    private final RestClient restClient;

    public PedidosAtivosClient(
            RestClient.Builder restClientBuilder,
            @Value("${clients.ms-pedidos.url}") String msPedidosUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(msPedidosUrl)
                .build();
    }

    public boolean possuiPedidosAtivos(Integer atendimentoId) {
        Boolean possuiPedidosAtivos = restClient.get()
                .uri("/api/pedidos/atendimentos/{atendimentoId}/possui-ativos", atendimentoId)
                .retrieve()
                .body(Boolean.class);

        return Boolean.TRUE.equals(possuiPedidosAtivos);
    }

    public ResumoContaAtendimentoResponse buscarResumoConta(Integer atendimentoId) {
        return restClient.get()
                .uri("/api/pedidos/atendimentos/{atendimentoId}/resumo-conta", atendimentoId)
                .retrieve()
                .body(ResumoContaAtendimentoResponse.class);
    }
}
