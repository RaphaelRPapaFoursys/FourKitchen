package br.com.fourkitchen.ms_pedidos.controller;

import br.com.fourkitchen.ms_pedidos.dto.request.AlterarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.CriarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.response.PedidoCozinhaResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.PedidoResponse;
import br.com.fourkitchen.ms_pedidos.exceptions.PedidoInexistenteException;
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

    @GetMapping("/atendimentos/{atendimentoId}/possui-ativos")
    private ResponseEntity<Boolean> possuiPedidosAtivos(
            @PathVariable Integer atendimentoId
    ) {
        return ResponseEntity.ok(pedidoService.possuiPedidosAtivos(atendimentoId));
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
}
