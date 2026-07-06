package br.com.fourkitchen.ms_pedidos.controller;

import br.com.fourkitchen.ms_pedidos.dto.request.AlterarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.CriarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.DecisaoProblemaRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.SinalizarProblemaRequest;
import br.com.fourkitchen.ms_pedidos.dto.response.PedidoCozinhaResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.PedidoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.SinalizarProblemaResponse;
import br.com.fourkitchen.ms_pedidos.exceptions.PedidoInexistenteException;
import br.com.fourkitchen.ms_pedidos.exceptions.PedidoNaoPermiteDecisaoException;
import br.com.fourkitchen.ms_pedidos.exceptions.ProdutoPedidoInexistenteException;
import br.com.fourkitchen.ms_pedidos.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("pedidos")
public class PedidoController {
    @Autowired
    PedidoService pedidoService;

    @PostMapping
    private ResponseEntity<PedidoResponse> criarPedido(
            @RequestBody @Valid CriarPedidoRequest pedidoRequest
    ) {
        PedidoResponse pedidoResponse = pedidoService.createPedido(pedidoRequest);

        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(pedidoResponse);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }

    @GetMapping("{id}")
    private ResponseEntity<PedidoResponse> buscarPedido(
            @PathVariable Integer id
    ) {
        PedidoResponse pedidoResponse = pedidoService.findById(id);

        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(pedidoResponse);
        } catch (Exception e) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    @GetMapping
    private ResponseEntity<List<PedidoResponse>> buscarPedidos() {
        List<PedidoResponse> listaPedidos = pedidoService.findAll();

        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(listaPedidos);
        } catch (Exception e) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    @GetMapping("/cozinha")
    private ResponseEntity<List<PedidoResponse>> buscarPedidosCozinha() {
        return ResponseEntity.ok(pedidoService.findPedidosCozinha());
    }

    @GetMapping("/cozinha/fila")
    private ResponseEntity<List<PedidoCozinhaResponse>> buscarFilaCozinha() {
        return ResponseEntity.ok(pedidoService.findFilaCozinha());
    }

    @PatchMapping("/{id}/iniciar-preparo")
    private ResponseEntity<PedidoResponse> iniciarPreparo(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.iniciarPreparo(id));
    }

    @PatchMapping("/{id}/finalizar-preparo")
    private ResponseEntity<PedidoResponse> finalizarPreparo(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.finalizarPreparo(id));
    }

    @GetMapping("/atendimentos/{atendimentoId}/possui-ativos")
    private ResponseEntity<Boolean> possuiPedidosAtivos(
            @PathVariable Integer atendimentoId
    ) {
        return ResponseEntity.ok(pedidoService.possuiPedidosAtivos(atendimentoId));
    }

    @GetMapping("/atendimentos/ativos")
    private ResponseEntity<List<PedidoResponse>> listarPedidosAtivosPorAtendimentos(
            @RequestParam("idsAtendimento") List<Integer> idsAtendimento
    ) {
        return ResponseEntity.ok(pedidoService.findPedidosAtivosPorAtendimentos(idsAtendimento));
    }

    @PatchMapping("{id}")
    private ResponseEntity<Void> alterarPedido(
            @PathVariable Integer id,
            @RequestBody AlterarPedidoRequest alterarPedidoRequest
    ) {
        pedidoService.patchPedido(id, alterarPedidoRequest);

        try {
            return ResponseEntity
                    .ok()
                    .build();
        } catch (PedidoInexistenteException e) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    @DeleteMapping("{id}")
    private ResponseEntity<Void> cancelarPedido(
            @PathVariable Integer id
    ) {
        pedidoService.softDelete(id);

        try {
            return ResponseEntity
                    .noContent()
                    .build();
        } catch (PedidoInexistenteException e) {
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

        } catch (PedidoInexistenteException | ProdutoPedidoInexistenteException e) {
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
        } catch (PedidoInexistenteException |
                 PedidoNaoPermiteDecisaoException error) {
            return ResponseEntity
                    .badRequest().build();
        }
    }
}
