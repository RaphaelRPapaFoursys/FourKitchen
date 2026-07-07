package br.com.fourkitchen.bff_restaurante.client.usuarios.dto;

public record UsuarioLoginResponse(
        String token,
        Long id,
        String nome,
        String useremail,
        String perfil,
        Integer idMesa
) {
}
