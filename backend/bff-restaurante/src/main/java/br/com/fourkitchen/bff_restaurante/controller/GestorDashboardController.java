package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.PeriodoDashboard;
import br.com.fourkitchen.bff_restaurante.dto.FiltroDashboard;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidosCanalResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ProblemasCozinhaMotivoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.RankingProdutosResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.VolumePedidosHorarioResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.GestorDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gestor/dashboard")
@Tag(name = "Dashboard do gestor", description = "Indicadores gráficos do acompanhamento operacional.")
@SecurityRequirement(name = "bearerAuth")
public class GestorDashboardController {

    private final GestorDashboardService service;

    @GetMapping("/pedidos-por-horario")
    @Operation(summary = "Volume de pedidos por horário", description = "Retorna os pedidos criados por hora e o primeiro horário de pico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados", content = @Content(schema = @Schema(implementation = VolumePedidosHorarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Período inválido", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "503", description = "Dados temporariamente indisponíveis", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<VolumePedidosHorarioResponse> pedidosPorHorario(
            @RequestParam @Parameter(required = true) PeriodoDashboard periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) String canal,
            @RequestParam(required = false) Integer idMesa,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(service.buscarPedidosPorHorario(filtro(periodo, dataInicial, dataFinal, canal, idMesa, status)));
    }

    @GetMapping("/problemas-por-motivo")
    @Operation(summary = "Problemas da cozinha por motivo", description = "Retorna ocorrências históricas da cozinha agrupadas pelo motivo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados", content = @Content(schema = @Schema(implementation = ProblemasCozinhaMotivoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Período inválido", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "503", description = "Dados temporariamente indisponíveis", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<ProblemasCozinhaMotivoResponse> problemasPorMotivo(
            @RequestParam @Parameter(required = true) PeriodoDashboard periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) String canal,
            @RequestParam(required = false) Integer idMesa,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(service.buscarProblemasPorMotivo(filtro(periodo, dataInicial, dataFinal, canal, idMesa, status)));
    }

    @GetMapping("/pedidos-por-canal")
    @Operation(summary = "Pedidos por canal", description = "Retorna a participação de Totem, Mesa e Garçom na demanda criada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados", content = @Content(schema = @Schema(implementation = PedidosCanalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Período inválido", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "503", description = "Dados temporariamente indisponíveis", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<PedidosCanalResponse> pedidosPorCanal(
            @RequestParam @Parameter(required = true) PeriodoDashboard periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) String canal,
            @RequestParam(required = false) Integer idMesa,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(service.buscarPedidosPorCanal(filtro(periodo, dataInicial, dataFinal, canal, idMesa, status)));
    }

    @GetMapping("/ranking-produtos")
    @Operation(summary = "Ranking dos produtos", description = "Retorna os cinco produtos mais pedidos no período selecionado.")
    public ResponseEntity<RankingProdutosResponse> rankingProdutos(
            @RequestParam PeriodoDashboard periodo
    ) {
        return ResponseEntity.ok(service.buscarRankingProdutos(periodo));
    }

    private FiltroDashboard filtro(
            PeriodoDashboard periodo,
            LocalDate dataInicial,
            LocalDate dataFinal,
            String canal,
            Integer idMesa,
            String status
    ) {
        return new FiltroDashboard(periodo, dataInicial, dataFinal, canal, idMesa, status);
    }

}
