package br.com.fourkitchen.ms_usuarios.mapper;

import br.com.fourkitchen.ms_usuarios.dto.request.CriarUsuarioRequest;
import br.com.fourkitchen.ms_usuarios.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class CriarUsuarioRequestMapper implements Mapper<CriarUsuarioRequest, Usuario> {
    @Override
    public Usuario map(CriarUsuarioRequest source) {
        return Usuario.builder()
                .nome(source.nome())
                .email(source.email())
                .senha(source.senha())
                .perfilUsuario(source.perfilUsuario())
                .idMesa(source.idMesa())
                .build();
    }
}
