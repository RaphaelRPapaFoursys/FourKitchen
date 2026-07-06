package br.com.fourkitchen.ms_usuarios.service;

import br.com.fourkitchen.ms_usuarios.dto.request.CriarUsuarioRequest;
import br.com.fourkitchen.ms_usuarios.dto.response.UsuarioResponse;
import br.com.fourkitchen.ms_usuarios.enums.PerfilUsuario;
import br.com.fourkitchen.ms_usuarios.exception.BaseException;
import br.com.fourkitchen.ms_usuarios.exception.ErrorEnum;
import br.com.fourkitchen.ms_usuarios.mapper.CriarUsuarioRequestMapper;
import br.com.fourkitchen.ms_usuarios.mapper.UsuarioResponseMapper;
import br.com.fourkitchen.ms_usuarios.model.Usuario;
import br.com.fourkitchen.ms_usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private final UsuarioResponseMapper usuarioResponseMapper;

    private final CriarUsuarioRequestMapper criarUsuarioRequestMapper;

    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> buscarUsuariosAtivos() {

        List<Usuario> usuarios = usuarioRepository.findByAtivoTrue();
        return usuarios.stream().map(usuarioResponseMapper::map).toList();
    }

    public UsuarioResponse criarUsuario(CriarUsuarioRequest request) {

        if (usuarioRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BaseException(ErrorEnum.EMAIL_JA_CADASTRADO);
        }

        validarVinculoMesa(request);

        Usuario usuario = criarUsuarioRequestMapper.map(request);

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setAtivo(true);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return usuarioResponseMapper.map(usuarioSalvo);

    }

    private void validarVinculoMesa(CriarUsuarioRequest request) {
        if (PerfilUsuario.MESA.equals(request.perfilUsuario())) {
            if (request.idMesa() == null || request.idMesa() <= 0) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            return;
        }

        if (request.idMesa() != null) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
    }

}
