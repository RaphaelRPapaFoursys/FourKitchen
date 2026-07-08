package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaCardapioResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.CardapioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Cardapio", description = "Rotas para exibir produtos disponiveis agrupados por categoria.")
@SecurityRequirement(name = "bearerAuth")
public class CardapioController {

    private final CardapioService cardapioService;

    @GetMapping("/mesa/cardapio")
    @Operation(
            summary = "Lista cardapio da mesa",
            description = "Retorna produtos disponiveis agrupados por categoria para o dispositivo de mesa."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cardapio retornado com sucesso",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = CategoriaCardapioResponse.class)),
                            examples = @ExampleObject(value = "[{\"categoriaId\":1,\"categoriaNome\":\"Lanches\",\"categoriaDescricao\":\"Sanduiches, porcoes e combinados\",\"categoriaImagem\":\"iVBORw0KGgoAAAANSUhEUgAA...\",\"produtos\":[{\"id\":10,\"nome\":\"X-Burger\",\"descricao\":\"Pao, carne, queijo e molho da casa\",\"imagem\":\"iVBORw0KGgoAAAANSUhEUgAA...\",\"preco\":29.90}]}]")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil MESA", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<CategoriaCardapioResponse>> buscarCardapioMesa() {
        return ResponseEntity.ok(cardapioService.buscarCardapio());
    }

    @GetMapping("/totem/cardapio")
    @Operation(
            summary = "Lista cardapio do totem",
            description = "Retorna produtos disponiveis agrupados por categoria para o totem."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cardapio retornado com sucesso",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = CategoriaCardapioResponse.class)),
                            examples = @ExampleObject(value = "[{\"categoriaId\":1,\"categoriaNome\":\"Lanches\",\"categoriaDescricao\":\"Sanduiches, porcoes e combinados\",\"categoriaImagem\":null,\"produtos\":[{\"id\":10,\"nome\":\"X-Burger\",\"descricao\":\"Pao, carne, queijo e molho da casa\",\"imagem\":null,\"preco\":29.90}]}]")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil TOTEM", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<CategoriaCardapioResponse>> buscarCardapioTotem() {
        return ResponseEntity.ok(cardapioService.buscarCardapio());
    }

    @GetMapping("/garcom/cardapio")
    @Operation(
            summary = "Lista cardapio do garcom",
            description = "Retorna produtos disponiveis agrupados por categoria para o garcom."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cardapio retornado com sucesso",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = CategoriaCardapioResponse.class)),
                            examples = @ExampleObject(value = "[{\"categoriaId\":1,\"categoriaNome\":\"Lanches\",\"categoriaDescricao\":\"Sanduiches, porcoes e combinados\",\"categoriaImagem\":null,\"produtos\":[{\"id\":10,\"nome\":\"X-Burger\",\"descricao\":\"Pao, carne, queijo e molho da casa\",\"imagem\":null,\"preco\":29.90}]}]")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GARCOM", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<CategoriaCardapioResponse>> buscarCardapioGarcom() {
        return ResponseEntity.ok(cardapioService.buscarCardapio());
    }
}
