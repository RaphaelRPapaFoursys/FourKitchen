package br.com.fourkitchen.ms_usuarios.dto.requestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Informe o email.")
        @Email(message = "Email invalido")
        String useremail,

        @NotBlank(message = "Informe a senha.")
        String password

) {}
