package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.TotemGestorResponse;
import br.com.fourkitchen.bff_restaurante.service.GestorTotemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gestor/totens")
@Tag(name = "Totens do Gestor", description = "Visao operacional dos totens cadastrados.")
@SecurityRequirement(name = "bearerAuth")
public class GestorTotemController {

    private final GestorTotemService gestorTotemService;

    @GetMapping
    @Operation(summary = "Lista totens", description = "Combina os acessos TOTEM ativos com os indicadores de pedidos do dia.")
    public ResponseEntity<List<TotemGestorResponse>> listarTotens(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorTotemService.listarTotens(authorization));
    }
}
