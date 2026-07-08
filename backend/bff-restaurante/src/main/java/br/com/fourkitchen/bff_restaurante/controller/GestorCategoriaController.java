package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarCategoriaGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarCategoriaGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaGestorResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.GestorCategoriaService;
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
@RequestMapping("/api/gestor/categorias")
@Tag(name = "Categorias do Gestor", description = "Rotas usadas pelo gestor para gerenciar categorias.")
@SecurityRequirement(name = "bearerAuth")
public class GestorCategoriaController {

    private final GestorCategoriaService gestorCategoriaService;

    @GetMapping
    @Operation(summary = "Lista categorias", description = "Retorna categorias cadastradas para gerenciamento do gestor.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Categorias retornadas com sucesso",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = CategoriaGestorResponse.class)),
                            examples = @ExampleObject(value = "[{\"id\":1,\"nome\":\"Lanches\",\"descricao\":\"Hamburgueres e sanduiches\",\"imagem\":\"iVBORw0KGgoAAAANSUhEUgAA...\",\"ativo\":true}]")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<CategoriaGestorResponse>> listarCategorias(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorCategoriaService.listarCategorias(authorization));
    }

    @PostMapping
    @Operation(
            summary = "Cria categoria",
            description = "Cria categoria com imagem opcional. A imagem aceita Base64 puro ou Data URL nos formatos JPG/JPEG/PNG, ate 1 MB, maximo 1200x900 e proporcao 4:3."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso", content = @Content(schema = @Schema(implementation = CategoriaGestorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou imagem fora do padrao", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "409", description = "Nome da categoria ja cadastrado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<CategoriaGestorResponse> criarCategoria(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Dados da categoria. O campo imagem e opcional.",
                    content = @Content(
                            schema = @Schema(implementation = CriarCategoriaGestorRequest.class),
                            examples = @ExampleObject(value = "{\"nome\":\"Lanches\",\"descricao\":\"Hamburgueres e sanduiches\",\"imagem\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...\"}")
                    )
            )
            @RequestBody @Valid CriarCategoriaGestorRequest request,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        CategoriaGestorResponse response = gestorCategoriaService.criarCategoria(request, authorization);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualiza categoria",
            description = "Atualiza categoria. Quando imagem vier null, a imagem atual e mantida; quando vier Base64, a imagem e substituida."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso", content = @Content(schema = @Schema(implementation = CategoriaGestorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou imagem fora do padrao", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Categoria nao encontrada", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "409", description = "Nome da categoria ja cadastrado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<CategoriaGestorResponse> atualizarCategoria(
            @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Dados atualizados da categoria.",
                    content = @Content(
                            schema = @Schema(implementation = AtualizarCategoriaGestorRequest.class),
                            examples = @ExampleObject(value = "{\"nome\":\"Lanches Artesanais\",\"descricao\":\"Hamburgueres e sanduiches\",\"imagem\":null}")
                    )
            )
            @RequestBody @Valid AtualizarCategoriaGestorRequest request,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorCategoriaService.atualizarCategoria(id, request, authorization));
    }

    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Ativa categoria", description = "Marca a categoria como ativa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria ativada com sucesso", content = @Content(schema = @Schema(implementation = CategoriaGestorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Categoria nao encontrada", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<CategoriaGestorResponse> ativarCategoria(
            @PathVariable Integer id,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorCategoriaService.ativarCategoria(id, authorization));
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativa categoria", description = "Faz exclusao logica da categoria, marcando ativo como false.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria desativada com sucesso", content = @Content(schema = @Schema(implementation = CategoriaGestorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Categoria nao encontrada", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<CategoriaGestorResponse> desativarCategoria(
            @PathVariable Integer id,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorCategoriaService.desativarCategoria(id, authorization));
    }
}
