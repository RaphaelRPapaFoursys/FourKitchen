package br.com.fourkitchen.bff_restaurante.client.usuarios.dto;

public record UsuarioClientResponse(
        Integer id,
        String nome,
        String email,
        String perfilUsuario,
        Boolean ativo
) {
}
