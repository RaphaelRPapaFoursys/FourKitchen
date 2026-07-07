package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.ResumoOperacaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.GestorResumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gestor/resumo")
@Tag(name = "Gestor", description = "Rotas de acompanhamento operacional para gestor.")
@SecurityRequirement(name = "bearerAuth")
public class GestorResumoController {

    private final GestorResumoService gestorResumoService;

    @GetMapping
    @Operation(
            summary = "Consulta resumo da operacao",
            description = "Retorna totais de pedidos em preparo, pedidos prontos, mesas ocupadas, problemas pendentes e chamadas pendentes."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumo retornado com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = ResumoOperacaoResponse.class),
                            examples = @ExampleObject(value = "{\"pedidosEmPreparo\":5,\"pedidosProntos\":3,\"mesasOcupadas\":8,\"problemasPendentes\":2,\"chamadasPendentes\":4}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, invalido ou expirado",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"002\",\"msgError\":\"Token invalido ou expirado\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuario sem perfil de gestor ou admin",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"003\",\"msgError\":\"Acesso negado\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Servico dependente indisponivel",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"008\",\"msgError\":\"Servico de pedidos indisponivel\"}")
                    )
            )
    })
    public ResponseEntity<ResumoOperacaoResponse> buscarResumo() {
        return ResponseEntity.ok(gestorResumoService.buscarResumo());
    }
}
