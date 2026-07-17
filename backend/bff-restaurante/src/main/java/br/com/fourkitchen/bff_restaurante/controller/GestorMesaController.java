package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.AtribuirGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.GarcomResumoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.HistoricoAtendimentoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGestorPaginadaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGestorResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoDetalheGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ResumoPainelResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.GestorMesaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gestor")
@Tag(name = "Mesas do Gestor", description = "Rotas usadas pelo gestor para visualizar e controlar mesas.")
@SecurityRequirement(name = "bearerAuth")
public class GestorMesaController {

    private final GestorMesaService gestorMesaService;

    @GetMapping("/mesas")
    @Operation(
            summary = "Lista mesas para o painel do gestor",
            description = "Retorna todas as mesas com numero, status, garcom atribuido e dados da sessao aberta."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Mesas retornadas com sucesso",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = MesaGestorResponse.class)),
                            examples = @ExampleObject(value = "[{\"id\":1,\"numero\":10,\"status\":\"OCUPADA\",\"garcomId\":7,\"garcomNome\":\"Amanda Souza\",\"codigoSessao\":123456,\"dataAbertura\":\"2026-07-02T10:00:00\",\"dataFechamento\":null},{\"id\":2,\"numero\":11,\"status\":\"DISPONIVEL\",\"garcomId\":null,\"garcomNome\":null,\"codigoSessao\":null,\"dataAbertura\":null,\"dataFechamento\":null}]")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de mesas ou usuarios indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<MesaGestorResponse>> listarMesas(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorMesaService.listarMesas(authorization));
    }

    @GetMapping("/mesas/{id}/pedidos")
    @Operation(
            summary = "Lista os pedidos detalhados de uma mesa",
            description = "Retorna código, status, progresso e itens dos pedidos do atendimento atual para o modal do gestor."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos detalhados retornados com sucesso", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PedidoDetalheGarcomResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Mesa sem atendimento aberto", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Mesa não encontrada", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Serviço de mesas ou pedidos indisponível", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<PedidoDetalheGarcomResponse>> listarPedidosDetalhados(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(gestorMesaService.listarPedidosDetalhados(id));
    }

    @GetMapping("/mesas/paginadas")
    @Operation(
            summary = "Lista mesas paginadas para o painel do gestor",
            description = "Retorna uma pagina de mesas com numero, status, garcom atribuido, dados da sessao aberta e metadados de paginacao."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pagina de mesas retornada com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = MesaGestorPaginadaResponse.class),
                            examples = @ExampleObject(value = "{\"content\":[{\"id\":1,\"numero\":10,\"status\":\"OCUPADA\",\"garcomId\":7,\"garcomNome\":\"Amanda Souza\",\"codigoSessao\":123456,\"dataAbertura\":\"2026-07-02T10:00:00\",\"dataFechamento\":null,\"pedidos\":[]}],\"page\":0,\"size\":10,\"totalElements\":48,\"totalPages\":5,\"first\":true,\"last\":false}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de mesas, pedidos ou usuarios indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<MesaGestorPaginadaResponse> listarMesasPaginadas(
            @Parameter(description = "Pagina solicitada, iniciando em zero", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Quantidade de itens por pagina", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Ordenacao: numero,asc; numero,desc; criticidade; valor,desc; valor,asc", example = "numero,asc")
            @RequestParam(defaultValue = "numero,asc") String sort,
            @Parameter(description = "Filtro de estado das mesas", example = "PRONTOS")
            @RequestParam(required = false) String filtroEstado,
            @Parameter(description = "Identificador do garcom atribuido", example = "7")
            @RequestParam(required = false) Integer garcomId,
            @Parameter(description = "Busca por numero, garcom ou status", example = "em preparo")
            @RequestParam(required = false) String busca,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorMesaService.listarMesasPaginadas(
                authorization,
                page,
                size,
                sort,
                filtroEstado,
                garcomId,
                busca
        ));
    }

    @GetMapping("/mesas/resumo")
    @Operation(
            summary = "Consulta KPIs do painel do gestor",
            description = "Retorna os KPIs de mesas e a carga por garcom considerando todas as mesas, sem filtros."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumo retornado com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = ResumoPainelResponse.class),
                            examples = @ExampleObject(value = "{\"mesasLivres\":12,\"emPreparo\":5,\"prontos\":3,\"problemas\":2,\"ticketMedio\":82.50,\"cargaGarcons\":[{\"id\":7,\"nome\":\"Amanda Souza\",\"mesasAtivas\":3}]}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de mesas, pedidos ou usuarios indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<ResumoPainelResponse> buscarResumoPainel(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorMesaService.buscarResumoPainel(authorization));
    }

    @GetMapping("/garcons")
    @Operation(
            summary = "Lista garcons ativos",
            description = "Retorna somente usuarios ativos com perfil GARCOM para preencher o seletor de atribuicao de mesa."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Garcons retornados com sucesso",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = GarcomResumoResponse.class)),
                            examples = @ExampleObject(value = "[{\"id\":7,\"nome\":\"Amanda Souza\",\"email\":\"amanda@fourkitchen.com\"}]")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de usuarios indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<GarcomResumoResponse>> listarGarcons(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorMesaService.listarGarcons(authorization));
    }

    @GetMapping("/atendimentos/historico")
    @Operation(
            summary = "Lista historico de atendimentos",
            description = "Retorna atendimentos finalizados com mesa, garcom, valor final da conta, duracao e horario de finalizacao."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Historico retornado com sucesso",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = HistoricoAtendimentoResponse.class)),
                            examples = @ExampleObject(value = "[{\"id\":1,\"idAtendimento\":8,\"codigoSessao\":123456,\"idMesa\":1,\"numeroMesa\":10,\"idGarcom\":7,\"nomeGarcom\":\"Amanda Souza\",\"valorFinal\":149.70,\"totalPedidos\":3,\"totalItens\":7,\"dataAbertura\":\"2026-07-02T10:00:00\",\"dataFechamento\":\"2026-07-02T11:20:00\",\"duracaoMinutos\":80}]")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de mesas ou usuarios indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<HistoricoAtendimentoResponse>> listarHistoricoAtendimentos(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorMesaService.listarHistoricoAtendimentos(authorization));
    }

    @PatchMapping("/mesas/{id}/abrir")
    @Operation(
            summary = "Abre mesa",
            description = "Abre uma mesa disponivel. O ms-mesas cria a sessao, gera codigo de sessao e altera o status para OCUPADA."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mesa aberta com sucesso", content = @Content(schema = @Schema(implementation = MesaGestorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Mesa nao disponivel ou regra de negocio invalida", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Mesa nao encontrada", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de mesas indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<MesaGestorResponse> abrirMesa(
            @PathVariable Integer id,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorMesaService.abrirMesa(id, authorization));
    }

    @PatchMapping("/mesas/{id}/fechar")
    @Operation(
            summary = "Fecha mesa",
            description = "Fecha uma mesa ocupada quando nao houver pedidos ativos. A mesa volta para DISPONIVEL."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mesa fechada com sucesso", content = @Content(schema = @Schema(implementation = MesaGestorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Mesa nao ocupada, mesa com pedidos ativos ou regra de negocio invalida", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Mesa nao encontrada", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de mesas indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<MesaGestorResponse> fecharMesa(
            @PathVariable Integer id,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorMesaService.fecharMesa(id, authorization));
    }

    @PatchMapping("/mesas/{id}/atribuir-garcom")
    @Operation(
            summary = "Atribui garcom a mesa",
            description = "Valida se o usuario informado e um garcom ativo e atribui esse garcom ao atendimento aberto da mesa."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Garcom atribuido com sucesso", content = @Content(schema = @Schema(implementation = MesaGestorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Garcom invalido, mesa sem atendimento aberto ou regra de negocio invalida", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Mesa nao encontrada", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de mesas ou usuarios indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<MesaGestorResponse> atribuirGarcom(
            @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Garcom selecionado pelo gestor para assumir a mesa.",
                    content = @Content(
                            schema = @Schema(implementation = AtribuirGarcomRequest.class),
                            examples = @ExampleObject(value = "{\"garcomId\":7}")
                    )
            )
            @RequestBody @Valid AtribuirGarcomRequest request,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorMesaService.atribuirGarcom(id, request, authorization));
    }

    @PatchMapping("/mesas/{id}/marcar-entregue")
    @Operation(
            summary = "Marca pedidos prontos como entregues",
            description = "Marca como entregues todos os pedidos da mesa que estiverem com status PRONTO."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos marcados como entregues com sucesso", content = @Content(schema = @Schema(implementation = MesaGestorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Mesa sem atendimento aberto", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Mesa nao encontrada", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de mesas ou pedidos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<MesaGestorResponse> marcarEntregue(
            @PathVariable Integer id,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorMesaService.marcarEntregue(id, authorization));
    }
}
