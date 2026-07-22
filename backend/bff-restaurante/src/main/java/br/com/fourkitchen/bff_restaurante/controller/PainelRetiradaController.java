package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.PedidoPainelRetiradaResponse;
import br.com.fourkitchen.bff_restaurante.service.RetiradaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/painel-retirada")
@Tag(name = "Painel de retirada", description = "Consulta publica e sem dados pessoais para o painel de retirada.")
public class PainelRetiradaController {

    private final RetiradaService retiradaService;

    @GetMapping("/pedidos")
    @Operation(summary = "Lista codigos de pedidos ativos do totem")
    public ResponseEntity<List<PedidoPainelRetiradaResponse>> listarPedidos() {
        return ResponseEntity.ok(retiradaService.listarPainelPublico());
    }
}
