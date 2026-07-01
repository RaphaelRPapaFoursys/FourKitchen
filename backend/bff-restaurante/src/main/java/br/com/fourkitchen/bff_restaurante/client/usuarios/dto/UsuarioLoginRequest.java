package br.com.fourkitchen.bff_restaurante.client.usuarios.dto;

public record UsuarioLoginRequest(
        String useremail,
        String password
) {
}
