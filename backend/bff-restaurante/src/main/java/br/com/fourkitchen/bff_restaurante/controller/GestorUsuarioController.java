package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarUsuarioRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.UsuarioGestorResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.GestorUsuarioService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gestor/usuarios")
@Tag(name = "Usuarios do Gestor", description = "Rotas usadas pelo gestor para gerenciar usuarios.")
@SecurityRequirement(name = "bearerAuth")
public class GestorUsuarioController {

    private final GestorUsuarioService gestorUsuarioService;

    @GetMapping
    @Operation(
            summary = "Lista usuarios ativos",
            description = "Retorna usuarios ativos para a tela de gerenciamento do gestor."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuarios retornados com sucesso",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = UsuarioGestorResponse.class)),
                            examples = @ExampleObject(value = "[{\"id\":1,\"nome\":\"Maria Silva\",\"email\":\"maria@fourkitchen.com\",\"perfilUsuario\":\"GESTOR\",\"idMesa\":null,\"ativo\":true}]")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de usuarios indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<List<UsuarioGestorResponse>> listarUsuarios(
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorUsuarioService.listarUsuarios(authorization));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualiza usuario",
            description = "Atualiza nome, email, perfil e mesa vinculada. A senha so e alterada quando enviada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario atualizado com sucesso", content = @Content(schema = @Schema(implementation = UsuarioGestorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "409", description = "Email ja utilizado por outro usuario", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de usuarios indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<UsuarioGestorResponse> atualizarUsuario(
            @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Dados atualizados do usuario.",
                    content = @Content(
                            schema = @Schema(implementation = AtualizarUsuarioRequest.class),
                            examples = @ExampleObject(value = "{\"nome\":\"Maria Silva\",\"email\":\"maria@fourkitchen.com\",\"senha\":\"NovaSenha123\",\"perfilUsuario\":\"GESTOR\",\"idMesa\":null}")
                    )
            )
            @RequestBody @Valid AtualizarUsuarioRequest request,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        return ResponseEntity.ok(gestorUsuarioService.atualizarUsuario(id, request, authorization));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Inativa usuario",
            description = "Faz exclusao logica do usuario, alterando ativo para false sem remover o registro do banco."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario inativado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Usuario ja inativo ou regra de negocio invalida", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem perfil GESTOR ou ADMIN ou tentando excluir a si mesmo", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content(schema = @Schema(implementation = ErrorObject.class))),
            @ApiResponse(responseCode = "502", description = "Servico de usuarios indisponivel", content = @Content(schema = @Schema(implementation = ErrorObject.class)))
    })
    public ResponseEntity<Void> inativarUsuario(
            @PathVariable Integer id,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Parameter(hidden = true) Authentication authentication
    ) {
        gestorUsuarioService.inativarUsuario(id, authorization, authentication);

        return ResponseEntity.noContent().build();
    }
}
