package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.MesaAtendimentoAtualResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.MesaAtendimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mesa")
@Tag(name = "Atendimento da Mesa", description = "Rotas usadas pelo tablet da mesa para descobrir o atendimento atual.")
@SecurityRequirement(name = "bearerAuth")
public class MesaAtendimentoController {

    private final MesaAtendimentoService mesaAtendimentoService;

    @GetMapping("/atendimento-atual")
    @Operation(
            summary = "Busca atendimento atual da mesa",
            description = "Identifica a mesa pelo token e retorna o id da mesa, atendimento aberto e codigoAtendimento usado pelo frontend."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Atendimento atual retornado com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = MesaAtendimentoAtualResponse.class),
                            examples = @ExampleObject(value = "{\"idMesa\":1,\"idAtendimento\":8,\"codigoAtendimento\":123456,\"status\":\"OCUPADA\"}")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Mesa sem atendimento aberto", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil MESA", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Mesa nao encontrada", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de mesas indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<MesaAtendimentoAtualResponse> buscarAtendimentoAtual(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(mesaAtendimentoService.buscarAtendimentoAtual(authentication));
    }
}
