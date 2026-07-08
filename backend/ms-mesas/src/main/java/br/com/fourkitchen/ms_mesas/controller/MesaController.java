package br.com.fourkitchen.ms_mesas.controller;

import br.com.fourkitchen.ms_mesas.dto.request.AtribuirGarcomRequest;
import br.com.fourkitchen.ms_mesas.dto.request.CriarMesaRequest;
import br.com.fourkitchen.ms_mesas.dto.response.HistoricoAtendimentoResponse;
import br.com.fourkitchen.ms_mesas.dto.response.MesaGarcomResponse;
import br.com.fourkitchen.ms_mesas.dto.response.MesaPaginadaResponse;
import br.com.fourkitchen.ms_mesas.dto.response.MesaResponse;
import br.com.fourkitchen.ms_mesas.dto.response.ResumoMesasOperacaoResponse;
import br.com.fourkitchen.ms_mesas.dto.response.SessaoMesaResponse;
import br.com.fourkitchen.ms_mesas.service.MesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    @PostMapping
    public ResponseEntity<MesaResponse> criarMesa(
            @RequestBody @Valid CriarMesaRequest request
    ) {
        MesaResponse response = mesaService.criarMesa(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MesaResponse>> listarMesas() {
        return ResponseEntity.ok(mesaService.listarMesas());
    }
    //devolve page
    @GetMapping("/paginadas")
    public ResponseEntity<MesaPaginadaResponse> listarMesasPaginadas(
            @PageableDefault(size = 10, sort = "numero", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(mesaService.listarMesasPaginadas(pageable));
    }

    @GetMapping("/garcons/{idGarcom}")
    public ResponseEntity<List<MesaGarcomResponse>> listarMesasPorGarcom(@PathVariable Integer idGarcom) {
        return ResponseEntity.ok(mesaService.listarMesasPorGarcom(idGarcom));
    }

    @GetMapping("/resumo-operacao")
    public ResponseEntity<ResumoMesasOperacaoResponse> buscarResumoOperacao() {
        return ResponseEntity.ok(mesaService.buscarResumoOperacao());
    }

    @GetMapping("/atendimentos/historico")
    public ResponseEntity<List<HistoricoAtendimentoResponse>> listarHistoricoAtendimentos() {
        return ResponseEntity.ok(mesaService.listarHistoricoAtendimentos());
    }

    @PatchMapping("/{id}/abrir")
    public ResponseEntity<MesaResponse> abrirMesa(@PathVariable Integer id) {
        return ResponseEntity.ok(mesaService.abrirMesa(id));
    }

    @PatchMapping("/{id}/fechar")
    public ResponseEntity<MesaResponse> fecharMesa(@PathVariable Integer id) {
        return ResponseEntity.ok(mesaService.fecharMesa(id));
    }

    @PatchMapping("/{id}/atribuir-garcom")
    public ResponseEntity<MesaResponse> atribuirGarcom(
            @PathVariable Integer id,
            @RequestBody @Valid AtribuirGarcomRequest request
    ) {
        return ResponseEntity.ok(mesaService.atribuirGarcom(id, request));
    } 

    @GetMapping("/{id}/sessoes/{codigoSessao}/validar")
    public ResponseEntity<SessaoMesaResponse> validarSessaoMesa(
            @PathVariable Integer id,
            @PathVariable Integer codigoSessao
    ) {
        return ResponseEntity.ok(mesaService.validarSessaoMesa(id, codigoSessao));
    }

    @GetMapping("/{id}/atendimento-atual")
    public ResponseEntity<SessaoMesaResponse> buscarAtendimentoAtual(@PathVariable Integer id) {
        return ResponseEntity.ok(mesaService.buscarAtendimentoAtual(id));
    }

    @GetMapping("/{id}/garcons/{idGarcom}/validar")
    public ResponseEntity<SessaoMesaResponse> validarMesaAtribuidaGarcom(
            @PathVariable Integer id,
            @PathVariable Integer idGarcom
    ) {
        return ResponseEntity.ok(mesaService.validarMesaAtribuidaGarcom(id, idGarcom));
    }
}
