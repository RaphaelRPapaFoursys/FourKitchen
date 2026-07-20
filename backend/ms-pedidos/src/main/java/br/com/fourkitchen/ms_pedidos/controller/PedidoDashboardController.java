package br.com.fourkitchen.ms_pedidos.controller;

import br.com.fourkitchen.ms_pedidos.dto.response.PedidosCanalResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.ProblemasCozinhaMotivoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.RankingProdutosResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.VolumePedidosHorarioResponse;
import br.com.fourkitchen.ms_pedidos.enums.PeriodoDashboard;
import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import br.com.fourkitchen.ms_pedidos.exceptions.ErrorObject;
import br.com.fourkitchen.ms_pedidos.service.PedidoDashboardService;
import br.com.fourkitchen.ms_pedidos.service.FiltroDashboard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/pedidos/dashboard")
@Tag(name = "Dashboard de pedidos", description = "Agregações operacionais de pedidos para consumo interno do BFF.")
public class PedidoDashboardController {

    private final PedidoDashboardService service;

    @GetMapping("/pedidos-por-horario")
    @Operation(summary = "Volume de pedidos por horário", description = "Agrupa pela hora de criação, incluindo pedidos cancelados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agregação retornada"),
            @ApiResponse(responseCode = "400", description = "Período inválido", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<VolumePedidosHorarioResponse> pedidosPorHorario(
            @Parameter(description = "ULTIMA_HORA, TURNO_ATUAL ou HOJE", required = true)
            @RequestParam PeriodoDashboard periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) CanaisPedido canal,
            @RequestParam(required = false) Integer idMesa,
            @RequestParam(required = false) StatusPedido status
    ) {
        return ResponseEntity.ok(service.buscarVolumePorHorario(filtro(periodo, dataInicial, dataFinal, canal, idMesa, status)));
    }

    @GetMapping("/problemas-por-motivo")
    @Operation(summary = "Problemas da cozinha por motivo", description = "Agrupa ocorrências pelo motivo e pela data de criação do problema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agregação retornada"),
            @ApiResponse(responseCode = "400", description = "Período inválido", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<ProblemasCozinhaMotivoResponse> problemasPorMotivo(
            @Parameter(description = "ULTIMA_HORA, TURNO_ATUAL ou HOJE", required = true)
            @RequestParam PeriodoDashboard periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) CanaisPedido canal,
            @RequestParam(required = false) Integer idMesa,
            @RequestParam(required = false) StatusPedido status
    ) {
        return ResponseEntity.ok(service.buscarProblemasPorMotivo(filtro(periodo, dataInicial, dataFinal, canal, idMesa, status)));
    }

    @GetMapping("/pedidos-por-canal")
    @Operation(summary = "Pedidos por canal", description = "Agrupa pedidos pelo canal de origem, incluindo cancelados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agregação retornada"),
            @ApiResponse(responseCode = "400", description = "Período inválido", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<PedidosCanalResponse> pedidosPorCanal(
            @Parameter(description = "ULTIMA_HORA, TURNO_ATUAL ou HOJE", required = true)
            @RequestParam PeriodoDashboard periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) CanaisPedido canal,
            @RequestParam(required = false) Integer idMesa,
            @RequestParam(required = false) StatusPedido status
    ) {
        return ResponseEntity.ok(service.buscarPedidosPorCanal(filtro(periodo, dataInicial, dataFinal, canal, idMesa, status)));
    }

    @GetMapping("/ranking-produtos")
    @Operation(summary = "Ranking dos produtos", description = "Retorna os cinco produtos mais pedidos, somando suas quantidades e desconsiderando pedidos e itens cancelados.")
    public ResponseEntity<RankingProdutosResponse> rankingProdutos(
            @RequestParam PeriodoDashboard periodo
    ) {
        return ResponseEntity.ok(service.buscarRankingProdutos(periodo));
    }

    private FiltroDashboard filtro(
            PeriodoDashboard periodo,
            LocalDate dataInicial,
            LocalDate dataFinal,
            CanaisPedido canal,
            Integer idMesa,
            StatusPedido status
    ) {
        return new FiltroDashboard(periodo, dataInicial, dataFinal, canal, idMesa, status);
    }
}
