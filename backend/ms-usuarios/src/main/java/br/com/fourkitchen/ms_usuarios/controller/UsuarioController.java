package br.com.fourkitchen.ms_usuarios.controller;

import br.com.fourkitchen.ms_usuarios.dto.request.AtualizarUsuarioRequest;
import br.com.fourkitchen.ms_usuarios.dto.request.CriarUsuarioRequest;
import br.com.fourkitchen.ms_usuarios.dto.response.UsuarioResponse;
import br.com.fourkitchen.ms_usuarios.exception.ErrorObject;
import br.com.fourkitchen.ms_usuarios.model.Usuario;
import br.com.fourkitchen.ms_usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Cadastro e consulta de usuarios.")
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping()
    @Operation(
            summary = "Lista usuarios ativos",
            description = "Retorna somente usuarios ativos.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<UsuarioResponse>> listarUsuariosAtivos() {

        return ResponseEntity.ok(usuarioService.buscarUsuariosAtivos());

    }

    @GetMapping("/todos")
    @Operation(
            summary = "Lista todos os usuarios",
            description = "Retorna usuarios ativos e inativos para a gestao de acessos.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.buscarUsuarios());
    }

    @PostMapping()
    @Operation(
            summary = "Cria usuario",
            description = "Cadastra um usuario ativo. A senha deve ter no minimo 8 caracteres, uma letra maiuscula, uma letra minuscula e um numero.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario criado com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados invalidos",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = @ExampleObject(value = "{\"codError\":\"004\",\"msgError\":\"A senha deve conter no minimo 8 caracteres, uma letra maiuscula, uma letra minuscula e um numero.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email ja cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            )
    })
    public ResponseEntity<UsuarioResponse> criarUsuario(
            @RequestBody @Valid CriarUsuarioRequest request
            ){

        UsuarioResponse response = usuarioService.criarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualiza usuario",
            description = "Atualiza nome, email, perfil e vinculo de mesa. A senha e alterada somente quando informada.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario atualizado com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = UsuarioResponse.class),
                            examples = @ExampleObject(value = "{\"id\":1,\"nome\":\"Maria Silva\",\"email\":\"maria@fourkitchen.com\",\"perfilUsuario\":\"GESTOR\",\"idMesa\":null,\"ativo\":true}")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil ADMIN ou GESTOR", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "409", description = "Email ja cadastrado para outro usuario", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<UsuarioResponse> atualizarUsuario(
            @PathVariable Integer id,
            @RequestBody @Valid AtualizarUsuarioRequest request
    ) {
        return ResponseEntity.ok(usuarioService.atualizarUsuario(id, request));
    }

    @PatchMapping("/{id}/ativar")
    @Operation(
            summary = "Ativa usuario",
            description = "Reativa um usuario que havia sido inativado.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UsuarioResponse> ativarUsuario(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.ativarUsuario(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Inativa usuario",
            description = "Executa exclusao logica do usuario, alterando ativo para false. Nao remove o registro do banco.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario inativado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Usuario ja esta inativo", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil ADMIN ou GESTOR ou tentando excluir a si mesmo", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<Void> inativarUsuario(
            @PathVariable Integer id,
            @Parameter(hidden = true) Authentication authentication
    ) {
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        usuarioService.inativarUsuario(id, usuarioAutenticado.getId());

        return ResponseEntity.noContent().build();
    }
}
