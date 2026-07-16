package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.DecisaoProblemaRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoProblemaTotemGarcomResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.GarcomTotemProblemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/garcom/pedidos-totem")
@Tag(name = "Problemas de pedidos TOTEM", description = "Fila coletiva de problemas de pedidos feitos no TOTEM.")
@SecurityRequirement(name = "bearerAuth")
public class GarcomTotemProblemaController {

    private final GarcomTotemProblemaService garcomTotemProblemaService;

    @GetMapping("/problemas")
    @Operation(summary = "Lista problemas de pedidos TOTEM", description = "Retorna pedidos TOTEM aguardando decisao. Casos sem responsavel ficam disponiveis para assuncao por qualquer garcom.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fila retornada com sucesso", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PedidoProblemaTotemGarcomResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GARCOM", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de pedidos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<PedidoProblemaTotemGarcomResponse>> listarProblemas(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(garcomTotemProblemaService.listarProblemas(authentication));
    }

    @PatchMapping("/{idPedido}/problemas/assumir")
    @Operation(summary = "Assume um problema de pedido TOTEM", description = "Assuncao atomica: o primeiro garcom a assumir torna-se o unico autorizado a registrar a decisao.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Problema assumido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Pedido nao aguarda decisao ou dados invalidos", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Pedido nao encontrado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "409", description = "Problema ja assumido ou resolvido por outro garcom", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de pedidos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<Void> assumir(
            @PathVariable Integer idPedido,
            @Parameter(hidden = true) Authentication authentication
    ) {
        garcomTotemProblemaService.assumir(idPedido, authentication);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{idPedido}/problemas/decisao")
    @Operation(summary = "Registra decisao para problema de pedido TOTEM", description = "Reutiliza a decisao de problema existente. Apenas o garcom que assumiu o caso pode remover, substituir ou cancelar o pedido.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Decisao registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Pedido ou item nao permite decisao", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Pedido nao encontrado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "409", description = "Problema nao foi assumido pelo garcom autenticado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de pedidos ou produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<Void> registrarDecisao(
            @PathVariable Integer idPedido,
            @RequestBody @Valid DecisaoProblemaRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        garcomTotemProblemaService.registrarDecisao(idPedido, request, authentication);
        return ResponseEntity.noContent().build();
    }
}
