package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.MesaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.FechamentoContaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGarcomDetalheResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoDetalheGarcomResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.GarcomMesaService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/garcom/mesas")
@Tag(name = "Mesas do Garcom", description = "Rotas usadas pelo garcom para visualizar suas mesas atribuidas.")
@SecurityRequirement(name = "bearerAuth")
public class GarcomMesaController {

    private final GarcomMesaService garcomMesaService;

    @GetMapping
    @Operation(
            summary = "Lista mesas atribuidas ao garcom logado",
            description = "Retorna apenas mesas atribuidas ao garcom autenticado, incluindo pedidos ativos e chamadas pendentes destacadas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Mesas do garcom retornadas com sucesso",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = MesaGarcomResponse.class)),
                            examples = @ExampleObject(value = "[{\"idMesa\":1,\"numero\":10,\"status\":\"OCUPADA\",\"idAtendimento\":8,\"codigoSessao\":123456,\"idGarcom\":7,\"dataAbertura\":\"2026-07-02T10:00:00\",\"pedidosAtivos\":[{\"id\":25,\"codigo\":100025,\"canal\":\"GARCOM\",\"status\":\"ENVIADO_COZINHA\",\"idAtendimento\":8}],\"chamadasPendentes\":[{\"id\":3,\"tipo\":\"CHAMADA_GARCOM\",\"mensagem\":\"Cliente solicitou atendimento\",\"data\":\"2026-07-02T10:15:30\"}],\"possuiChamadaPendente\":true}]")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, invalido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuario sem perfil GARCOM",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Servico de mesas, pedidos ou notificacoes indisponivel",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            )
    })
    public ResponseEntity<List<MesaGarcomResponse>> listarMesas(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(garcomMesaService.listarMesas(authentication));
    }

    @GetMapping("/{id}/detalhe")
    @Operation(
            summary = "Detalha mesa atribuida ao garcom logado",
            description = "Valida se a mesa pertence ao garcom autenticado e retorna mesa, conta, pedidos detalhados e problemas pendentes de decisao."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhe retornado com sucesso", content = @Content(schema = @Schema(implementation = MesaGarcomDetalheResponse.class))),
            @ApiResponse(responseCode = "400", description = "Mesa sem atendimento aberto ou dados invalidos", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Mesa nao atribuida ao garcom", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de mesas ou pedidos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<MesaGarcomDetalheResponse> detalharMesa(
            @PathVariable Integer id,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(garcomMesaService.detalharMesa(id, authentication));
    }

    @GetMapping("/{id}/pedidos")
    @Operation(
            summary = "Lista pedidos detalhados da mesa do garcom",
            description = "Valida se a mesa pertence ao garcom autenticado e retorna pedidos ativos e historicos do atendimento atual."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos retornados com sucesso", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PedidoDetalheGarcomResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Mesa sem atendimento aberto ou dados invalidos", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Mesa nao atribuida ao garcom", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de mesas ou pedidos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<PedidoDetalheGarcomResponse>> listarPedidosDaMesa(
            @PathVariable Integer id,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(garcomMesaService.listarPedidosDaMesa(id, authentication));
    }

    @PatchMapping("/{id}/fechar-conta")
    @Operation(
            summary = "Fecha conta da mesa atribuida ao garcom",
            description = "Valida se a mesa pertence ao garcom autenticado, bloqueia fechamento com pedidos ativos e finaliza o atendimento da mesa."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta fechada com sucesso", content = @Content(schema = @Schema(implementation = FechamentoContaGarcomResponse.class))),
            @ApiResponse(responseCode = "400", description = "Conta nao pode ser fechada ou mesa sem atendimento aberto", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Mesa nao atribuida ao garcom", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de mesas ou pedidos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<FechamentoContaGarcomResponse> fecharConta(
            @PathVariable Integer id,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(garcomMesaService.fecharConta(id, authentication));
    }
}
