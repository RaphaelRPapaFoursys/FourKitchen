package br.com.fourkitchen.ms_usuarios.dto.response;

import br.com.fourkitchen.ms_usuarios.enums.PerfilUsuario;

public record UsuarioResponse(
        Integer id,
        String nome,
        String email,
        PerfilUsuario perfilUsuario,
        Boolean ativo
) {
}
