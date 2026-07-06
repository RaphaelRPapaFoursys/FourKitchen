package br.com.fourkitchen.ms_usuarios.dto.responseDto;

public record LoginResponse(
        String token,
        Long id,
        String nome,
        String useremail,
        String perfil,
        Integer idMesa
) {}
