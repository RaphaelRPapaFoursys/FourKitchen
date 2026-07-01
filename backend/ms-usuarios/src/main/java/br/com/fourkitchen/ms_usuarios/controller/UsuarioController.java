package br.com.fourkitchen.ms_usuarios.controller;

import br.com.fourkitchen.ms_usuarios.dto.request.CriarUsuarioRequest;
import br.com.fourkitchen.ms_usuarios.dto.response.UsuarioResponse;
import br.com.fourkitchen.ms_usuarios.exception.ErrorObject;
import br.com.fourkitchen.ms_usuarios.service.UsuarioService;
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
}
