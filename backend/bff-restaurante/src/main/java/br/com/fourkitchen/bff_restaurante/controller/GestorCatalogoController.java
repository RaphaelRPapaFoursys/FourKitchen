package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarCategoriaGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarProdutoGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarCategoriaGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarProdutoGestorRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaGestorResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ProdutoGestorResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ProdutoGestorPaginadoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaGestorPaginadaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaOpcaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.GestorCatalogoService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gestor/catalogo")
@Tag(name = "Catalogo do Gestor", description = "Rotas usadas pelo gestor/admin para gerenciar produtos e categorias.")
@SecurityRequirement(name = "bearerAuth")
public class GestorCatalogoController {

    private static final String REGRAS_IMAGEM = "Imagem opcional em Base64 puro ou Data URL Base64. "
            + "Quando enviada, deve ser JPG/JPEG ou PNG, ate 1 MB, dimensoes maximas de 1200x900 e proporcao 4:3.";

    private final GestorCatalogoService gestorCatalogoService;

    @GetMapping("/produtos")
    @Operation(
            summary = "Lista produtos para gestao",
            description = "Retorna uma pagina de produtos, incluindo indisponiveis, sem incluir os bytes das imagens no JSON."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Produtos retornados com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = ProdutoGestorPaginadoResponse.class),
                            examples = @ExampleObject(value = "{\"content\":[{\"id\":10,\"nome\":\"Risoto de cogumelos\",\"descricao\":\"Arroz arboreo com mix de cogumelos e parmesao\",\"imagemUrl\":\"/api/produtos/10/imagem?v=1784635200000\",\"preco\":58.90,\"categoriaId\":1,\"categoria\":\"Pratos principais\",\"disponivel\":true}],\"page\":0,\"size\":10,\"totalElements\":1,\"totalPages\":1,\"first\":true,\"last\":true}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<ProdutoGestorPaginadoResponse> listarProdutos(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "busca", required = false) String busca,
            @RequestParam(name = "categoriaId", required = false) Integer categoriaId
    ) {
        return ResponseEntity.ok(gestorCatalogoService.listarProdutos(page, size, busca, categoriaId));
    }

    @PostMapping("/produtos")
    @Operation(
            summary = "Cadastra produto",
            description = "Cadastra um produto em uma categoria ativa. " + REGRAS_IMAGEM
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso", content = @Content(schema = @Schema(implementation = ProdutoGestorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos, imagem invalida ou categoria inativa", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Categoria nao encontrada", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<ProdutoGestorResponse> criarProduto(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Dados do produto. " + REGRAS_IMAGEM,
                    content = @Content(
                            schema = @Schema(implementation = CriarProdutoGestorRequest.class),
                            examples = @ExampleObject(value = "{\"nome\":\"Risoto de cogumelos\",\"descricao\":\"Arroz arboreo com mix de cogumelos e parmesao\",\"imagem\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...\",\"preco\":58.90,\"categoriaId\":1}")
                    )
            )
            @RequestBody @Valid CriarProdutoGestorRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestorCatalogoService.criarProduto(request));
    }

    @PutMapping("/produtos/{id}")
    @Operation(
            summary = "Atualiza produto",
            description = "Atualiza nome, descricao, imagem, preco e categoria do produto. Quando imagem for null, a imagem atual e mantida. " + REGRAS_IMAGEM
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso", content = @Content(schema = @Schema(implementation = ProdutoGestorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos, imagem invalida ou categoria inativa", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Produto ou categoria nao encontrado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<ProdutoGestorResponse> atualizarProduto(
            @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Dados atualizados do produto. Envie imagem como null para manter a imagem atual.",
                    content = @Content(
                            schema = @Schema(implementation = AtualizarProdutoGestorRequest.class),
                            examples = @ExampleObject(value = "{\"nome\":\"Risoto de cogumelos\",\"descricao\":\"Arroz arboreo com mix de cogumelos e parmesao\",\"imagem\":null,\"preco\":58.90,\"categoriaId\":1}")
                    )
            )
            @RequestBody @Valid AtualizarProdutoGestorRequest request
    ) {
        return ResponseEntity.ok(gestorCatalogoService.atualizarProduto(id, request));
    }

    @PatchMapping("/produtos/{id}/ativar")
    @Operation(summary = "Ativa produto", description = "Reativa um produto indisponivel, marcando disponivel como true.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto ativado com sucesso", content = @Content(schema = @Schema(implementation = ProdutoGestorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<ProdutoGestorResponse> ativarProduto(@PathVariable Integer id) {
        return ResponseEntity.ok(gestorCatalogoService.ativarProduto(id));
    }

    @PatchMapping("/produtos/{id}/desativar")
    @Operation(summary = "Desativa produto", description = "Faz exclusao logica do produto, marcando disponivel como false sem remover o registro do banco.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto desativado com sucesso", content = @Content(schema = @Schema(implementation = ProdutoGestorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Produto nao encontrado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<ProdutoGestorResponse> desativarProduto(@PathVariable Integer id) {
        return ResponseEntity.ok(gestorCatalogoService.desativarProduto(id));
    }

    @GetMapping("/categorias")
    @Operation(
            summary = "Lista categorias para gestao",
            description = "Retorna uma pagina de categorias, incluindo inativas, sem incluir os bytes das imagens no JSON."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Categorias retornadas com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = CategoriaGestorPaginadaResponse.class),
                            examples = @ExampleObject(value = "{\"content\":[{\"id\":1,\"nome\":\"Pratos principais\",\"descricao\":\"Refeicoes principais servidas no restaurante\",\"imagemUrl\":\"/api/categorias/1/imagem?v=1784635200000\",\"ativo\":true}],\"page\":0,\"size\":10,\"totalElements\":1,\"totalPages\":1,\"first\":true,\"last\":true}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<CategoriaGestorPaginadaResponse> listarCategorias(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "busca", required = false) String busca,
            @RequestParam(name = "ativo", required = false) Boolean ativo
    ) {
        return ResponseEntity.ok(gestorCatalogoService.listarCategorias(page, size, busca, ativo));
    }

    @GetMapping("/categorias/opcoes")
    @Operation(summary = "Lista opcoes de categoria", description = "Retorna id, nome e status das categorias sem carregar imagens.")
    public ResponseEntity<List<CategoriaOpcaoResponse>> listarOpcoesCategorias() {
        return ResponseEntity.ok(gestorCatalogoService.listarOpcoesCategorias());
    }

    @PostMapping("/categorias")
    @Operation(
            summary = "Cadastra categoria",
            description = "Cadastra uma categoria ativa para organizar produtos. " + REGRAS_IMAGEM
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria cadastrada com sucesso", content = @Content(schema = @Schema(implementation = CategoriaGestorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou imagem invalida", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "409", description = "Nome de categoria ja cadastrado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<CategoriaGestorResponse> criarCategoria(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Dados da categoria. " + REGRAS_IMAGEM,
                    content = @Content(
                            schema = @Schema(implementation = CriarCategoriaGestorRequest.class),
                            examples = @ExampleObject(value = "{\"nome\":\"Pratos principais\",\"descricao\":\"Refeicoes principais servidas no restaurante\",\"imagem\":\"data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ...\"}")
                    )
            )
            @RequestBody @Valid CriarCategoriaGestorRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestorCatalogoService.criarCategoria(request));
    }

    @PutMapping("/categorias/{id}")
    @Operation(
            summary = "Atualiza categoria",
            description = "Atualiza nome, descricao e imagem da categoria. Quando imagem for null, a imagem atual e mantida. " + REGRAS_IMAGEM
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso", content = @Content(schema = @Schema(implementation = CategoriaGestorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou imagem invalida", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Categoria nao encontrada", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "409", description = "Nome de categoria ja cadastrado por outra categoria", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<CategoriaGestorResponse> atualizarCategoria(
            @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Dados atualizados da categoria. Envie imagem como null para manter a imagem atual.",
                    content = @Content(
                            schema = @Schema(implementation = AtualizarCategoriaGestorRequest.class),
                            examples = @ExampleObject(value = "{\"nome\":\"Pratos principais\",\"descricao\":\"Refeicoes principais servidas no restaurante\",\"imagem\":null}")
                    )
            )
            @RequestBody @Valid AtualizarCategoriaGestorRequest request
    ) {
        return ResponseEntity.ok(gestorCatalogoService.atualizarCategoria(id, request));
    }

    @PatchMapping("/categorias/{id}/ativar")
    @Operation(summary = "Ativa categoria", description = "Reativa uma categoria inativa, marcando ativo como true.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria ativada com sucesso", content = @Content(schema = @Schema(implementation = CategoriaGestorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Categoria nao encontrada", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<CategoriaGestorResponse> ativarCategoria(@PathVariable Integer id) {
        return ResponseEntity.ok(gestorCatalogoService.ativarCategoria(id));
    }

    @PatchMapping("/categorias/{id}/desativar")
    @Operation(summary = "Desativa categoria", description = "Faz exclusao logica da categoria, marcando ativo como false sem remover o registro do banco.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria desativada com sucesso", content = @Content(schema = @Schema(implementation = CategoriaGestorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Categoria nao encontrada", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de produtos indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<CategoriaGestorResponse> desativarCategoria(@PathVariable Integer id) {
        return ResponseEntity.ok(gestorCatalogoService.desativarCategoria(id));
    }
}
