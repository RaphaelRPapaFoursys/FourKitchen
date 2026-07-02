package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.NotificacaoService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notificacoes")
@Tag(name = "Notificacoes", description = "Rotas de notificacoes consumidas pelos frontends via BFF.")
@SecurityRequirement(name = "bearerAuth")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @PostMapping
    @Operation(
            summary = "Cria notificacao",
            description = "Cria uma notificacao no ms-notificacoes. O frontend informa tipo e destino; a mensagem e gerada automaticamente pelo tipo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Notificacao criada com sucesso",
                    content = @Content(schema = @Schema(implementation = NotificacaoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, invalido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "ms-notificacoes indisponivel",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"010\",\"msgError\":\"Servico de notificacoes indisponivel\"}")
                    )
            )
    })
    public ResponseEntity<NotificacaoResponse> criarNotificacao(
            @RequestBody @Valid CriarNotificacaoRequest request
    ) {
        NotificacaoResponse response = notificacaoService.criarNotificacao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/pendentes")
    @Operation(
            summary = "Lista notificacoes pendentes",
            description = "Lista notificacoes nao lidas. Quando destino for informado, filtra por GARCOM, MESA, TOTEM, COZINHA ou GESTOR."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notificacoes pendentes retornadas com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificacaoResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, invalido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "ms-notificacoes indisponivel",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            )
    })
    public ResponseEntity<List<NotificacaoResponse>> listarPendentes(
            @RequestParam(required = false) DestinoNotificacao destino
    ) {
        return ResponseEntity.ok(notificacaoService.listarPendentes(destino));
    }

    @PatchMapping("/{id}/lida")
    @Operation(
            summary = "Marca notificacao como lida",
            description = "Atualiza uma notificacao pendente para lida no ms-notificacoes."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notificacao marcada como lida",
                    content = @Content(schema = @Schema(implementation = NotificacaoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, invalido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
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
    public ResponseEntity<NotificacaoResponse> marcarComoLida(@PathVariable Integer id) {
        return ResponseEntity.ok(notificacaoService.marcarComoLida(id));
    }
}
