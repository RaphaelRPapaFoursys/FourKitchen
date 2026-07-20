package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.SinalizarProblemaRequest;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.SinalizarProblemaResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.DecisaoProblemaRequest;
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
import org.springframework.http.HttpHeaders;
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
                            examples = @ExampleObject(value = "[{\"id\":25,\"codigo\":100025,\"canal\":\"MESA\",\"status\":\"ENVIADO_COZINHA\",\"idMesa\":1,\"idAtendimento\":8,\"dataCriacao\":\"2026-07-02T10:30:00\",\"itens\":[{\"id\":5,\"idProduto\":10,\"nomeProduto\":\"X-Burger\",\"quantidade\":2,\"precoUnitario\":29.90,\"observacao\":\"Sem cebola\"}]}]")
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
    public ResponseEntity<List<PedidoFilaCozinhaResponse>> listarFila(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(cozinhaService.listarFila(authorization));
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
    @Operation(
            summary = "Sinaliza problema em um item de pedido",
            description = "Disponível somente para pedidos em ENVIADO_COZINHA, antes do início do preparo. Altera o status do pedido para AGUARDANDO_DECISAO e o status do item para ERRO ou INDISPONIVEL. Também cria uma notificação sobre o problema para o canal apropriado.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para sinalizar o problema. O status do produto deve ser um dos valores permitidos para problema.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SinalizarProblemaRequest.class),
                            examples = @ExampleObject(value = "{\"idPedido\": 1, \"idProdutoPedido\": 10, \"statusProdutoPedido\": \"INDISPONIVEL\"}")
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Problema sinalizado com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = SinalizarProblemaResponse.class),
                            examples = @ExampleObject(value = "{\"idPedido\":1,\"idProdutoPedido\":10,\"statusPedido\":\"AGUARDANDO_DECISAO\",\"statusProdutoPedido\":\"INDISPONIVEL\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de requisição inválidos ou pedido fora de ENVIADO_COZINHA; não é permitido sinalizar problema após o início do preparo.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"005\",\"msgError\":\"Status do pedido não permite sinalizar problema\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, inválido ou expirado.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"002\",\"msgError\":\"Token invalido ou expirado\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não tem permissão para acessar este recurso.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"003\",\"msgError\":\"Acesso negado\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido ou item do pedido não encontrado no ms-pedidos.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"014\",\"msgError\":\"Pedido nao encontrado\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Ocorreu uma falha de comunicação com um serviço interno (ms-pedidos ou ms-notificacoes).",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = {
                                    @ExampleObject(name = "Pedidos Indisponível", value = "{\"codError\":\"008\",\"msgError\":\"Servico de pedidos indisponivel\"}"),
                                    @ExampleObject(name = "Notificações Indisponível", value = "{\"codError\":\"010\",\"msgError\":\"Servico de notificacoes indisponivel\"}")
                            }
                    )
            )
    })
    public ResponseEntity<SinalizarProblemaResponse> sinalizarProblema(
            @RequestBody @Valid SinalizarProblemaRequest request
    ) {
        return ResponseEntity.ok(cozinhaService.sinalizarProblema(request));
    }

    @PatchMapping("/pedidos/decisao-problema")
    @Operation(
            summary = "Registra decisão sobre problema do pedido",
            description = "Permite cancelar o pedido, remover um item ou substituir um item. Após uma decisão válida, o pedido retorna para ENVIADO_COZINHA, exceto quando cancelado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Decisão processada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou regra de negócio não atendida",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, inválido ou expirado",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário sem permissão para realizar a ação",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido ou produto do pedido não encontrado",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Serviço de produtos indisponível",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class)
                    )
            )
    })
    public ResponseEntity<Void> decisaoProblema(
            @RequestBody @Valid DecisaoProblemaRequest decisaoProblemaRequest
    ) {
        cozinhaService.decisaoProblema(decisaoProblemaRequest);

        return ResponseEntity.ok().build();
    }
}
