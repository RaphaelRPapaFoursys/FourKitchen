package br.com.fourkitchen.ms_usuarios.mapper;

import br.com.fourkitchen.ms_usuarios.dto.response.UsuarioResponse;
import br.com.fourkitchen.ms_usuarios.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioResponseMapper implements Mapper<Usuario, UsuarioResponse> {

    @Override
    public UsuarioResponse  map(Usuario source) {
        return new UsuarioResponse(
                source.getId(),
                source.getNome(),
                source.getEmail(),
                source.getPerfilUsuario(),
                source.getIdMesa(),
                source.getAtivo()
        );
    }
}
