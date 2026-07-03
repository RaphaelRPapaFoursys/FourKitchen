package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.SinalizarProblemaRequest;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.SinalizarProblemaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoFilaCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoStatusCozinhaResponse;
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
import org.springframework.web.bind.annotation.*;

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
            description = "Retorna pedidos ENVIADO_COZINHA e EM_PREPARO em ordem de chegada. Pedidos PRONTO, ENTREGUE, FINALIZADO e CANCELADO nao aparecem."
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

    @PatchMapping("/pedidos/{id}/iniciar")
    @Operation(
            summary = "Inicia preparo do pedido",
            description = "Altera pedido de ENVIADO_COZINHA para EM_PREPARO e registra evento PEDIDO_EM_PREPARO."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido atualizado com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = PedidoStatusCozinhaResponse.class),
                            examples = @ExampleObject(value = "{\"id\":25,\"codigo\":100025,\"canal\":\"GARCOM\",\"status\":\"EM_PREPARO\",\"idMesa\":1,\"idAtendimento\":8}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Transicao de status invalida",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"015\",\"msgError\":\"Transicao de status invalida\"}")
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
                    responseCode = "404",
                    description = "Pedido nao encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Servico de pedidos ou notificacoes indisponivel",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            )
    })
    public ResponseEntity<PedidoStatusCozinhaResponse> iniciarPreparo(@PathVariable Integer id) {
        return ResponseEntity.ok(cozinhaService.iniciarPreparo(id));
    }

    @PatchMapping("/pedidos/{id}/finalizar")
    @Operation(
            summary = "Finaliza preparo do pedido",
            description = "Altera pedido de EM_PREPARO para PRONTO, remove da fila ativa e registra evento PEDIDO_PRONTO."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido atualizado com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = PedidoStatusCozinhaResponse.class),
                            examples = @ExampleObject(value = "{\"id\":25,\"codigo\":100025,\"canal\":\"GARCOM\",\"status\":\"PRONTO\",\"idMesa\":1,\"idAtendimento\":8}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Transicao de status invalida",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"015\",\"msgError\":\"Transicao de status invalida\"}")
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
                    responseCode = "404",
                    description = "Pedido nao encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Servico de pedidos ou notificacoes indisponivel",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            )
    })
    public ResponseEntity<PedidoStatusCozinhaResponse> finalizarPreparo(@PathVariable Integer id) {
        return ResponseEntity.ok(cozinhaService.finalizarPreparo(id));
    }

    @PatchMapping("/pedidos/sinalizar-problema")
    public ResponseEntity<SinalizarProblemaResponse> sinalizarProblema(
            @RequestBody @Valid SinalizarProblemaRequest request
    ) {
        return ResponseEntity.ok(cozinhaService.sinalizarProblema(request));
    }
}
