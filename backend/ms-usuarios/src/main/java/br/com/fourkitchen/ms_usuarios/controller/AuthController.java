package br.com.fourkitchen.ms_usuarios.controller;

import br.com.fourkitchen.ms_usuarios.dto.requestDto.LoginRequest;
import br.com.fourkitchen.ms_usuarios.dto.responseDto.LoginResponse;
import br.com.fourkitchen.ms_usuarios.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody LoginRequest request) {
        authService.criarUsuario(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário criado com sucesso.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.username(), request.password()));
    }
}