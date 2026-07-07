package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoTotemRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoTotemResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.TotemPedidoService;
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
@RequestMapping("/api/totem/pedidos")
@Tag(name = "Pedidos do Totem", description = "Rotas usadas pelo totem para criar pedidos.")
public class TotemPedidoController {

    private final TotemPedidoService totemPedidoService;

    @PostMapping
    @Operation(
            summary = "Cria pedido pelo totem",
            description = "Valida a disponibilidade dos produtos, usa o preco atual do ms-produtos e cria um pedido sem mesa com canal TOTEM enviado para a cozinha."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido criado com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = PedidoTotemResponse.class),
                            examples = @ExampleObject(value = "{\"id\":25,\"codigo\":100025,\"canal\":\"TOTEM\",\"status\":\"ENVIADO_COZINHA\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Produto indisponivel ou dados invalidos",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"011\",\"msgError\":\"Produto indisponivel\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Servico de produtos ou pedidos indisponivel",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"012\",\"msgError\":\"Servico de produtos indisponivel\"}")
                    )
            )
    })
    public ResponseEntity<PedidoTotemResponse> criarPedido(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Itens escolhidos no totem. O preco nao deve ser enviado pelo front; o BFF busca o preco atual no ms-produtos.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CriarPedidoTotemRequest.class),
                            examples = @ExampleObject(value = "{\"itens\":[{\"idProduto\":10,\"quantidade\":2,\"observacao\":\"Sem cebola\"}]}")
                    )
            )
            @RequestBody @Valid CriarPedidoTotemRequest request
    ) {
        PedidoTotemResponse response = totemPedidoService.criarPedido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
