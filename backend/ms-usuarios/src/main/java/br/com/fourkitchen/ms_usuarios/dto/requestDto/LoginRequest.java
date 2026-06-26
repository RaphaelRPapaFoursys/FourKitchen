package br.com.fourkitchen.ms_usuarios.dto.requestDto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Informe o usuário.")
        String useremail,

        @NotBlank(message = "Informe a senha.")
        String password

) {}