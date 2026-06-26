package br.com.fourkitchen.ms_usuarios.controller;

import br.com.fourkitchen.ms_usuarios.dto.request.CriarUsuarioRequest;
import br.com.fourkitchen.ms_usuarios.dto.response.UsuarioResponse;
import br.com.fourkitchen.ms_usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping()
    public ResponseEntity<List<UsuarioResponse>> listarUsuariosAtivos() {

        return ResponseEntity.ok(usuarioService.buscarUsuariosAtivos());

    }

    @PostMapping()
    public ResponseEntity<UsuarioResponse> criarUsuario(
            @RequestBody @Valid CriarUsuarioRequest request
            ){

        UsuarioResponse response = usuarioService.criarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}
