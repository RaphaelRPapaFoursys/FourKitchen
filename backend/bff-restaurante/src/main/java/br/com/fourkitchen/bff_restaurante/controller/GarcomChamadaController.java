package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.GarcomChamadaService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/garcom/chamadas")
@Tag(name = "Chamadas do Garcom", description = "Rotas usadas pelo garcom para concluir chamadas feitas pelas mesas.")
@SecurityRequirement(name = "bearerAuth")
public class GarcomChamadaController {

    private final GarcomChamadaService garcomChamadaService;

    @PatchMapping("/{id}/concluir")
    @Operation(
            summary = "Conclui chamada de garcom",
            description = "Marca uma chamada de garcom como lida quando ela pertence ao garcom autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Chamada concluida com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = NotificacaoResponse.class),
                            examples = @ExampleObject(value = "{\"id\":3,\"tipo\":\"CHAMADA_GARCOM\",\"mensagem\":\"Cliente solicitou atendimento\",\"destino\":\"GARCOM\",\"lida\":true,\"data\":\"2026-07-02T10:15:30\",\"idMesa\":1,\"idAtendimento\":8,\"idGarcom\":7}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Chamada invalida ou ja concluida",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"017\",\"msgError\":\"Chamada de garcom invalida\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, invalido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Chamada pertence a outro garcom ou usuario sem perfil GARCOM",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"018\",\"msgError\":\"Chamada de garcom nao pertence ao garcom\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notificacao nao encontrada",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"009\",\"msgError\":\"Notificacao nao encontrada\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "ms-notificacoes indisponivel",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            )
    })
    public ResponseEntity<NotificacaoResponse> concluirChamada(
            @PathVariable Integer id,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(garcomChamadaService.concluirChamada(id, authentication));
    }
}
