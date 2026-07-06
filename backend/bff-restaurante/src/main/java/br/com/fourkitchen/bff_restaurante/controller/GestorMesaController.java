package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.AtribuirGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.GarcomResumoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGestorResponse;
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
}
