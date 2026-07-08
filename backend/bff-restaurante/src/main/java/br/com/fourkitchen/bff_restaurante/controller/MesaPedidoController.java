package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoMesaRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoMesaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoMesaStatusResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.MesaPedidoService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mesa/pedidos")
@Tag(name = "Pedidos da Mesa", description = "Rotas usadas pelo tablet da mesa para criar e acompanhar pedidos.")
@SecurityRequirement(name = "bearerAuth")
public class MesaPedidoController {

    private final MesaPedidoService mesaPedidoService;

    @PostMapping
    @Operation(
            summary = "Cria pedido pela mesa",
            description = "Identifica a mesa pelo token, valida o codigoAtendimento, consulta o preco atual no ms-produtos e cria um pedido com canal MESA enviado para a cozinha. O campo legado codigoSessao tambem e aceito como alias."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido criado com sucesso",
                    content = @Content(schema = @Schema(implementation = PedidoMesaResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Sessao da mesa invalida, produto indisponivel ou dados invalidos",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"006\",\"msgError\":\"Sessao da mesa invalida\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Servico de mesas, produtos ou pedidos indisponivel",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"008\",\"msgError\":\"Servico de pedidos indisponivel\"}")
                    )
            )
    })
    public ResponseEntity<PedidoMesaResponse> criarPedido(
            @RequestBody @Valid CriarPedidoMesaRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        PedidoMesaResponse response = mesaPedidoService.criarPedido(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Lista pedidos do atendimento atual da mesa",
            description = "Identifica a mesa pelo token, valida o codigo de atendimento/sessao e retorna os pedidos vinculados ao atendimento atual da mesa."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedidos do atendimento retornados com sucesso",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = PedidoMesaStatusResponse.class)),
                            examples = @ExampleObject(value = "[{\"id\":25,\"codigo\":100025,\"canal\":\"MESA\",\"status\":\"ENVIADO_COZINHA\",\"idMesa\":1,\"idAtendimento\":8,\"codigoAtendimento\":123456,\"dataCriacao\":\"2026-07-02T10:30:00\",\"itens\":[{\"idProduto\":10,\"nome\":null,\"quantidade\":2,\"observacao\":\"Sem cebola\"}]}]")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Codigo de atendimento invalido ou sessao da mesa invalida",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"006\",\"msgError\":\"Sessao da mesa invalida\"}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil MESA", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de mesas ou pedidos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<PedidoMesaStatusResponse>> listarPedidosDoAtendimentoAtual(
            @Parameter(description = "Codigo do atendimento/sessao exibido no tablet da mesa", example = "123456")
            @RequestParam("codigoAtendimento") Integer codigoAtendimento,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(
                mesaPedidoService.listarPedidosDoAtendimentoAtual(codigoAtendimento, authentication)
        );
    }
}
