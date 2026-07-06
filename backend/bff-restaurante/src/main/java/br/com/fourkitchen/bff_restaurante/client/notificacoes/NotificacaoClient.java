package br.com.fourkitchen.bff_restaurante.client.notificacoes;

import br.com.fourkitchen.bff_restaurante.client.notificacoes.dto.ResumoNotificacoesOperacaoResponse;
import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "ms-notificacoes", url = "${clients.ms-notificacoes.url}")
public interface NotificacaoClient {

    @PostMapping("/api/notificacoes")
    NotificacaoResponse criarNotificacao(@RequestBody CriarNotificacaoRequest request);

    @GetMapping("/api/notificacoes/pendentes")
    List<NotificacaoResponse> listarPendentes(@RequestParam(required = false) DestinoNotificacao destino);

    @GetMapping("/api/notificacoes/chamadas-pendentes")
    List<NotificacaoResponse> listarChamadasPendentesPorAtendimentos(
            @RequestParam("idsAtendimento") List<Integer> idsAtendimento
    );

    @PatchMapping("/api/notificacoes/{id}/lida")
    NotificacaoResponse marcarComoLida(@PathVariable Integer id);

    @PatchMapping("/api/notificacoes/chamadas-garcom/{id}/concluir")
    NotificacaoResponse concluirChamadaGarcom(
            @PathVariable Integer id,
            @RequestParam Integer idGarcom
    );

    @GetMapping("/api/notificacoes/resumo-operacao")
    ResumoNotificacoesOperacaoResponse buscarResumoOperacao();
}
