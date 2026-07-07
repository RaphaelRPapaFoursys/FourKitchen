package br.com.fourkitchen.bff_restaurante.security;

public record UsuarioAutenticado(
        Long id,
        String nome,
        String email,
        String perfil,
        Integer idMesa
) {
}
