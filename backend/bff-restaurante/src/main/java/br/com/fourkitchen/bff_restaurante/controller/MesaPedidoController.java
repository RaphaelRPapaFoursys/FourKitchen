package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoMesaRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoMesaResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.MesaPedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mesa/pedidos")
@Tag(name = "Pedidos da Mesa", description = "Rotas usadas pelo tablet da mesa para criar pedidos.")
public class MesaPedidoController {

    private final MesaPedidoService mesaPedidoService;

    @PostMapping
    @Operation(
            summary = "Cria pedido pela mesa",
            description = "Valida o codigo de sessao da mesa e cria um pedido com canal MESA enviado para a cozinha."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido criado com sucesso",
                    content = @Content(schema = @Schema(implementation = PedidoMesaResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Sessao da mesa invalida ou dados invalidos",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"006\",\"msgError\":\"Sessao da mesa invalida\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Servico de mesas ou pedidos indisponivel",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"008\",\"msgError\":\"Servico de pedidos indisponivel\"}")
                    )
            )
    })
    public ResponseEntity<PedidoMesaResponse> criarPedido(
            @RequestBody @Valid CriarPedidoMesaRequest request
    ) {
        PedidoMesaResponse response = mesaPedidoService.criarPedido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
