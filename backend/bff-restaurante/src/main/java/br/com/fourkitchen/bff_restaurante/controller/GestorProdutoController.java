package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarProdutoGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarProdutoGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.ProdutoGestorResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.GestorProdutoService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gestor/produtos")
@Tag(name = "Produtos do Gestor", description = "Rotas usadas pelo gestor para gerenciar produtos.")
@SecurityRequirement(name = "bearerAuth")
public class GestorProdutoController {

    private final GestorProdutoService gestorProdutoService;

    @GetMapping
    @Operation(summary = "Lista produtos", description = "Retorna produtos cadastrados para gerenciamento do gestor.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Produtos retornados com sucesso",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ProdutoGestorResponse.class)),
                            examples = @ExampleObject(value = "[{\"id\":10,\"nome\":\"X-Burger\",\"descricao\":\"Pao, carne, queijo e molho da casa\",\"imagem\":\"iVBORw0KGgoAAAANSUhEUgAA...\",\"preco\":29.90,\"categoriaId\":1,\"categoria\":\"Lanches\",\"disponivel\":true}]")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<ProdutoGestorResponse>> listarProdutos(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorProdutoService.listarProdutos(authorization));
    }

    @PostMapping
    @Operation(
            summary = "Cria produto",
            description = "Cria produto com imagem opcional. A imagem aceita Base64 puro ou Data URL nos formatos JPG/JPEG/PNG, ate 1 MB, maximo 1200x900 e proporcao 4:3."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso", content = @Content(schema = @Schema(implementation = ProdutoGestorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos, categoria inativa ou imagem fora do padrao", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Categoria nao encontrada", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<ProdutoGestorResponse> criarProduto(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Dados do produto. O campo imagem e opcional.",
                    content = @Content(
                            schema = @Schema(implementation = CriarProdutoGestorRequest.class),
                            examples = @ExampleObject(value = "{\"nome\":\"X-Burger\",\"descricao\":\"Pao, carne, queijo e molho da casa\",\"imagem\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...\",\"preco\":29.90,\"categoriaId\":1}")
                    )
            )
            @RequestBody @Valid CriarProdutoGestorRequest request,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        ProdutoGestorResponse response = gestorProdutoService.criarProduto(request, authorization);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualiza produto",
            description = "Atualiza produto. Quando imagem vier null, a imagem atual e mantida; quando vier Base64, a imagem e substituida."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso", content = @Content(schema = @Schema(implementation = ProdutoGestorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos, categoria inativa ou imagem fora do padrao", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Produto ou categoria nao encontrado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<ProdutoGestorResponse> atualizarProduto(
            @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Dados atualizados do produto.",
                    content = @Content(
                            schema = @Schema(implementation = AtualizarProdutoGestorRequest.class),
                            examples = @ExampleObject(value = "{\"nome\":\"X-Burger Duplo\",\"descricao\":\"Pao, duas carnes e queijo\",\"imagem\":null,\"preco\":39.90,\"categoriaId\":1}")
                    )
            )
            @RequestBody @Valid AtualizarProdutoGestorRequest request,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorProdutoService.atualizarProduto(id, request, authorization));
    }

    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Ativa produto", description = "Marca o produto como disponivel para venda.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto ativado com sucesso", content = @Content(schema = @Schema(implementation = ProdutoGestorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<ProdutoGestorResponse> ativarProduto(
            @PathVariable Integer id,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorProdutoService.ativarProduto(id, authorization));
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativa produto", description = "Faz exclusao logica do produto, marcando como indisponivel.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto desativado com sucesso", content = @Content(schema = @Schema(implementation = ProdutoGestorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<ProdutoGestorResponse> desativarProduto(
            @PathVariable Integer id,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorProdutoService.desativarProduto(id, authorization));
    }
}
