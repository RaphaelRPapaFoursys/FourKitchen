package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.CardapioPaginadoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaCardapioResumoResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
                            examples = @ExampleObject(value = "[{\"categoriaId\":1,\"categoriaNome\":\"Lanches\",\"categoriaDescricao\":\"Sanduiches, porcoes e combinados\",\"produtos\":[{\"id\":10,\"nome\":\"X-Burger\",\"descricao\":\"Pao, carne, queijo e molho da casa\",\"imagemUrl\":\"/api/produtos/10/imagem\",\"preco\":29.90}]}]")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil MESA", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<CategoriaCardapioResponse>> buscarCardapioMesa() {
        return ResponseEntity.ok(cardapioService.buscarCardapio());
    }

    @GetMapping("/mesa/categorias")
    public ResponseEntity<List<CategoriaCardapioResumoResponse>> buscarCategoriasMesa() {
        return ResponseEntity.ok(cardapioService.buscarCategoriasAtivas());
    }

    @GetMapping("/mesa/cardapio/paginado")
    @Operation(
            summary = "Lista pagina do cardapio da mesa",
            description = "Retorna uma pagina de produtos disponiveis agrupados por categoria para carregamento progressivo."
    )
    public ResponseEntity<CardapioPaginadoResponse> buscarCardapioMesaPaginado(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "12") Integer size,
            @RequestParam(name = "categoriaId", required = false) Integer categoriaId
    ) {
        return ResponseEntity.ok(cardapioService.buscarCardapioPaginado(page, size, categoriaId));
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
                            examples = @ExampleObject(value = "[{\"categoriaId\":1,\"categoriaNome\":\"Lanches\",\"categoriaDescricao\":\"Sanduiches, porcoes e combinados\",\"produtos\":[{\"id\":10,\"nome\":\"X-Burger\",\"descricao\":\"Pao, carne, queijo e molho da casa\",\"imagemUrl\":\"/api/produtos/10/imagem\",\"preco\":29.90}]}]")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil TOTEM", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<CategoriaCardapioResponse>> buscarCardapioTotem() {
        return ResponseEntity.ok(cardapioService.buscarCardapio());
    }

    @GetMapping("/totem/categorias")
    public ResponseEntity<List<CategoriaCardapioResumoResponse>> buscarCategoriasTotem() {
        return ResponseEntity.ok(cardapioService.buscarCategoriasAtivas());
    }

    @GetMapping("/totem/cardapio/paginado")
    @Operation(
            summary = "Lista pagina do cardapio do totem",
            description = "Retorna uma pagina de produtos disponiveis agrupados por categoria para carregamento progressivo."
    )
    public ResponseEntity<CardapioPaginadoResponse> buscarCardapioTotemPaginado(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "12") Integer size,
            @RequestParam(name = "categoriaId", required = false) Integer categoriaId
    ) {
        return ResponseEntity.ok(cardapioService.buscarCardapioPaginado(page, size, categoriaId));
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
                            examples = @ExampleObject(value = "[{\"categoriaId\":1,\"categoriaNome\":\"Lanches\",\"categoriaDescricao\":\"Sanduiches, porcoes e combinados\",\"produtos\":[{\"id\":10,\"nome\":\"X-Burger\",\"descricao\":\"Pao, carne, queijo e molho da casa\",\"imagemUrl\":\"/api/produtos/10/imagem\",\"preco\":29.90}]}]")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GARCOM", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<CategoriaCardapioResponse>> buscarCardapioGarcom() {
        return ResponseEntity.ok(cardapioService.buscarCardapio());
    }

    @GetMapping("/garcom/categorias")
    public ResponseEntity<List<CategoriaCardapioResumoResponse>> buscarCategoriasGarcom() {
        return ResponseEntity.ok(cardapioService.buscarCategoriasAtivas());
    }

    @GetMapping("/garcom/cardapio/paginado")
    @Operation(
            summary = "Lista pagina do cardapio do garcom",
            description = "Retorna uma pagina de produtos disponiveis agrupados por categoria para carregamento progressivo."
    )
    public ResponseEntity<CardapioPaginadoResponse> buscarCardapioGarcomPaginado(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "12") Integer size,
            @RequestParam(name = "categoriaId", required = false) Integer categoriaId
    ) {
        return ResponseEntity.ok(cardapioService.buscarCardapioPaginado(page, size, categoriaId));
    }

    @GetMapping("/produtos/{id}/imagem")
    @Operation(
            summary = "Busca imagem de produto",
            description = "Retorna a imagem binaria do produto para carregamento progressivo e cache do navegador."
    )
    public ResponseEntity<byte[]> buscarImagemProduto(@PathVariable Integer id) {
        return cardapioService.buscarImagemProduto(id);
    }
}
