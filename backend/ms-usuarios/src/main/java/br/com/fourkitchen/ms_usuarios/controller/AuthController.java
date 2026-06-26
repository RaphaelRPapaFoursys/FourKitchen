package br.com.fourkitchen.ms_usuarios.controller;

import br.com.fourkitchen.ms_usuarios.dto.requestDto.LoginRequest;
import br.com.fourkitchen.ms_usuarios.dto.responseDto.LoginResponse;
import br.com.fourkitchen.ms_usuarios.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.useremail(), request.password()));
    }
}