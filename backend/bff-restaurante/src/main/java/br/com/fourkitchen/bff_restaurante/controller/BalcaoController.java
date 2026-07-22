package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.PedidoBalcaoResponse;
import br.com.fourkitchen.bff_restaurante.service.RetiradaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/balcao/pedidos")
@Tag(name = "Balcao", description = "Acompanhamento e entrega de pedidos do totem.")
public class BalcaoController {

    private final RetiradaService retiradaService;

    @GetMapping
    @Operation(summary = "Lista a fila ativa de pedidos do totem")
    public ResponseEntity<List<PedidoBalcaoResponse>> listarFila() {
        return ResponseEntity.ok(retiradaService.listarFilaBalcao());
    }

    @PatchMapping("/{id}/entregar")
    @Operation(summary = "Marca um pedido pronto do totem como entregue")
    public ResponseEntity<PedidoBalcaoResponse> entregar(@PathVariable Integer id) {
        return ResponseEntity.ok(retiradaService.entregar(id));
    }
}
