package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.AlterarStatusPedidoCozinhaRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoFilaCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.CozinhaService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cozinha")
@Tag(name = "Cozinha", description = "Rotas operacionais da cozinha.")
@SecurityRequirement(name = "bearerAuth")
public class CozinhaController {

    private final CozinhaService cozinhaService;

    @GetMapping("/fila")
    @Operation(
            summary = "Lista fila da cozinha",
            description = "Retorna pedidos ENVIADO_COZINHA, EM_PREPARO e PRONTO em ordem de chegada. Pedidos ENTREGUE, FINALIZADO e CANCELADO nao aparecem."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fila retornada com sucesso",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = PedidoFilaCozinhaResponse.class)),
                            examples = @ExampleObject(value = "[{\"id\":25,\"codigo\":100025,\"canal\":\"MESA\",\"status\":\"ENVIADO_COZINHA\",\"idMesa\":1,\"idAtendimento\":8,\"dataCriacao\":\"2026-07-02T10:30:00\",\"itens\":[{\"id\":5,\"idProduto\":10,\"quantidade\":2,\"precoUnitario\":29.90,\"observacao\":\"Sem cebola\"}]}]")
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
                    description = "Usuario sem perfil de cozinha",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"003\",\"msgError\":\"Acesso negado\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "ms-pedidos indisponivel",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"008\",\"msgError\":\"Servico de pedidos indisponivel\"}")
                    )
            )
    })
    public ResponseEntity<List<PedidoFilaCozinhaResponse>> listarFila() {
        return ResponseEntity.ok(cozinhaService.listarFila());
    }

    @PatchMapping("/pedidos/{id}/status")
    @Operation(
            summary = "Altera status de pedido da cozinha",
            description = "Permite que a cozinha mova pedidos para EM_PREPARO ou PRONTO."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status alterado com sucesso"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status invalido",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"004\",\"msgError\":\"Dados invalidos\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, invalido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuario sem perfil de cozinha",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "ms-pedidos indisponivel",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            )
    })
    public ResponseEntity<Void> alterarStatus(
            @PathVariable Integer id,
            @Valid @RequestBody AlterarStatusPedidoCozinhaRequest request
    ) {
        cozinhaService.alterarStatus(id, request);
        return ResponseEntity.noContent().build();
    }
}
