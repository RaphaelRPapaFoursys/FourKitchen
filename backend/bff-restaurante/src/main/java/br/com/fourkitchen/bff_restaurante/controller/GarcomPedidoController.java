package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoGarcomResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.GarcomPedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/garcom/pedidos")
@Tag(name = "Pedidos do Garcom", description = "Rotas usadas pelo garcom para criar pedidos em mesas atribuidas a ele.")
@SecurityRequirement(name = "bearerAuth")
public class GarcomPedidoController {

    private final GarcomPedidoService garcomPedidoService;

    @PostMapping
    @Operation(
            summary = "Cria pedido pelo garcom",
            description = "Valida se a mesa esta ocupada e atribuida ao garcom autenticado, cria o pedido com canal GARCOM e envia para a cozinha."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido criado com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = PedidoGarcomResponse.class),
                            examples = @ExampleObject(value = "{\"id\":25,\"codigo\":100025,\"canal\":\"GARCOM\",\"status\":\"ENVIADO_COZINHA\",\"idMesa\":1,\"idGarcom\":7,\"idAtendimento\":8}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Mesa nao ocupada ou dados invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, invalido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Mesa nao atribuida ao garcom autenticado",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"013\",\"msgError\":\"Mesa nao atribuida ao garcom\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Servico de mesas ou pedidos indisponivel",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            )
    })
    public ResponseEntity<PedidoGarcomResponse> criarPedido(
            @RequestBody @Valid CriarPedidoGarcomRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        PedidoGarcomResponse response = garcomPedidoService.criarPedido(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
