package br.com.fourkitchen.bff_restaurante.client.usuarios.dto;

public record CriarUsuarioClientRequest(
        String nome,
        String email,
        String senha,
        String perfilUsuario,
        Integer idMesa
) {
}
