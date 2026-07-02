package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.dto.request.AtribuirGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarMesaRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaResponse;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaClient mesaClient;

    @PostMapping
    public ResponseEntity<MesaResponse> criarMesa(
            @RequestBody @Valid CriarMesaRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mesaClient.criarMesa(request));
    }

    @GetMapping
    public ResponseEntity<List<MesaResponse>> listarMesas() {
        return ResponseEntity.ok(mesaClient.listarMesas());
    }

    @PatchMapping("/{id}/abrir")
    public ResponseEntity<MesaResponse> abrirMesa(@PathVariable Integer id) {
        return ResponseEntity.ok(mesaClient.abrirMesa(id));
    }

    @PatchMapping("/{id}/fechar")
    public ResponseEntity<MesaResponse> fecharMesa(@PathVariable Integer id) {
        return ResponseEntity.ok(mesaClient.fecharMesa(id));
    }

    @PatchMapping("/{id}/atribuir-garcom")
    public ResponseEntity<MesaResponse> atribuirGarcom(
            @PathVariable Integer id,
            @RequestBody @Valid AtribuirGarcomRequest request
    ) {
        return ResponseEntity.ok(mesaClient.atribuirGarcom(id, request));
    }
}
