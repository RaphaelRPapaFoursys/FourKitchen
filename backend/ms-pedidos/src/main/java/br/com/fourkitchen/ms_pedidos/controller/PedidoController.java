package br.com.fourkitchen.ms_pedidos.controller;

import br.com.fourkitchen.ms_pedidos.dto.request.AlterarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.CriarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.DecisaoProblemaRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.SinalizarProblemaRequest;
import br.com.fourkitchen.ms_pedidos.dto.response.PedidoCozinhaResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.PedidoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.ResumoPedidosOperacaoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.SinalizarProblemaResponse;
import br.com.fourkitchen.ms_pedidos.exceptions.BaseException;
import br.com.fourkitchen.ms_pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponse> criarPedido(
            @RequestBody @Valid CriarPedidoRequest pedidoRequest
    ) {
        PedidoResponse pedidoResponse = pedidoService.createPedido(pedidoRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pedidoResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPedido(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(pedidoService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> buscarPedidos() {
        return ResponseEntity.ok(pedidoService.findAll());
    }

    @GetMapping("/cozinha")
    public ResponseEntity<List<PedidoResponse>> buscarPedidosCozinha() {
        return ResponseEntity.ok(pedidoService.findPedidosCozinha());
    }

    @GetMapping("/cozinha/fila")
    public ResponseEntity<List<PedidoCozinhaResponse>> buscarFilaCozinha() {
        return ResponseEntity.ok(pedidoService.findFilaCozinha());
    }

    @PatchMapping("/{id}/iniciar-preparo")
    public ResponseEntity<PedidoResponse> iniciarPreparo(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.iniciarPreparo(id));
    }

    @PatchMapping("/{id}/finalizar-preparo")
    public ResponseEntity<PedidoResponse> finalizarPreparo(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.finalizarPreparo(id));
    }

    @PatchMapping("/{id}/entregar")
    public ResponseEntity<PedidoResponse> entregarPedido(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.entregarPedido(id));
    }

    @GetMapping("/atendimentos/{atendimentoId}/possui-ativos")
    public ResponseEntity<Boolean> possuiPedidosAtivos(
            @PathVariable Integer atendimentoId
    ) {
        return ResponseEntity.ok(pedidoService.possuiPedidosAtivos(atendimentoId));
    }

    @GetMapping("/atendimentos/ativos")
    public ResponseEntity<List<PedidoResponse>> listarPedidosAtivosPorAtendimentos(
            @RequestParam("idsAtendimento") List<Integer> idsAtendimento
    ) {
        return ResponseEntity.ok(pedidoService.findPedidosAtivosPorAtendimentos(idsAtendimento));
    }

    @GetMapping("/resumo-operacao")
    private ResponseEntity<ResumoPedidosOperacaoResponse> buscarResumoOperacao() {
        return ResponseEntity.ok(pedidoService.buscarResumoOperacao());
    }

    @GetMapping("/atendimentos/ativos/detalhado")
    public ResponseEntity<List<PedidoCozinhaResponse>> listarPedidosAtivosDetalhadosPorAtendimentos(
            @RequestParam("idsAtendimento") List<Integer> idsAtendimento
    ) {
        return ResponseEntity.ok(pedidoService.findPedidosAtivosDetalhadosPorAtendimentos(idsAtendimento));
    }

    @GetMapping("/atendimentos/{atendimentoId}/detalhado")
    public ResponseEntity<List<PedidoCozinhaResponse>> listarPedidosDetalhadosPorAtendimento(
            @PathVariable Integer atendimentoId
    ) {
        return ResponseEntity.ok(pedidoService.findPedidosDetalhadosPorAtendimento(atendimentoId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> alterarPedido(
            @PathVariable Integer id,
            @RequestBody AlterarPedidoRequest alterarPedidoRequest
    ) {
        pedidoService.patchPedido(id, alterarPedidoRequest);

        try {
            return ResponseEntity
                    .ok()
                    .build();
        } catch (BaseException e) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarPedido(
            @PathVariable Integer id
    ) {
        pedidoService.softDelete(id);

        try {
            return ResponseEntity
                    .noContent()
                    .build();
        } catch (BaseException error) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    @PatchMapping("/sinalizar-problema")
    public ResponseEntity<SinalizarProblemaResponse> sinalizarProblema(
            @RequestBody @Valid SinalizarProblemaRequest request
    ) {
        try {
            SinalizarProblemaResponse response = pedidoService.sinalizarProblema(request);
            return ResponseEntity.ok(response);

        } catch (BaseException error) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/decisao-problema")
    public ResponseEntity<Void> decisaoProblema(
            @RequestBody DecisaoProblemaRequest decisaoProblemaRequest
    ) {
        try{
            pedidoService.decisaoProblema(decisaoProblemaRequest);

            return ResponseEntity.ok().build();
        } catch (BaseException error) {
            return ResponseEntity
                    .badRequest().build();
        }
    }
}
