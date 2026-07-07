package br.com.fourkitchen.ms_notificacoes.controller;

import br.com.fourkitchen.ms_notificacoes.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.ms_notificacoes.dto.response.NotificacaoResponse;
import br.com.fourkitchen.ms_notificacoes.dto.response.ResumoNotificacoesOperacaoResponse;
import br.com.fourkitchen.ms_notificacoes.enums.DestinoNotificacao;
import br.com.fourkitchen.ms_notificacoes.service.NotificacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @PostMapping
    public ResponseEntity<NotificacaoResponse> criarNotificacao(
            @RequestBody @Valid CriarNotificacaoRequest request
    ) {
        NotificacaoResponse response = notificacaoService.criarNotificacao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<NotificacaoResponse>> listarPendentes(
            @RequestParam(required = false) DestinoNotificacao destino
    ) {
        return ResponseEntity.ok(notificacaoService.listarPendentes(destino));
    }

    @GetMapping("/chamadas-pendentes")
    public ResponseEntity<List<NotificacaoResponse>> listarChamadasPendentesPorAtendimentos(
            @RequestParam("idsAtendimento") List<Integer> idsAtendimento
    ) {
        return ResponseEntity.ok(notificacaoService.listarChamadasPendentesPorAtendimentos(idsAtendimento));
    }

    @GetMapping("/resumo-operacao")
    public ResponseEntity<ResumoNotificacoesOperacaoResponse> buscarResumoOperacao() {
        return ResponseEntity.ok(notificacaoService.buscarResumoOperacao());
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<NotificacaoResponse> marcarComoLida(@PathVariable Integer id) {
        return ResponseEntity.ok(notificacaoService.marcarComoLida(id));
    }

    @PatchMapping("/chamadas-garcom/{id}/concluir")
    public ResponseEntity<NotificacaoResponse> concluirChamadaGarcom(
            @PathVariable Integer id,
            @RequestParam Integer idGarcom
    ) {
        return ResponseEntity.ok(notificacaoService.concluirChamadaGarcom(id, idGarcom));
    }
}
