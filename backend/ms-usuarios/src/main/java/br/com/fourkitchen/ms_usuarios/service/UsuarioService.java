package br.com.fourkitchen.ms_usuarios.service;

import br.com.fourkitchen.ms_usuarios.dto.request.CriarUsuarioRequest;
import br.com.fourkitchen.ms_usuarios.dto.request.AtualizarUsuarioRequest;
import br.com.fourkitchen.ms_usuarios.dto.response.UsuarioResponse;
import br.com.fourkitchen.ms_usuarios.enums.PerfilUsuario;
import br.com.fourkitchen.ms_usuarios.exception.BaseException;
import br.com.fourkitchen.ms_usuarios.exception.ErrorEnum;
import br.com.fourkitchen.ms_usuarios.mapper.CriarUsuarioRequestMapper;
import br.com.fourkitchen.ms_usuarios.mapper.UsuarioResponseMapper;
import br.com.fourkitchen.ms_usuarios.model.Usuario;
import br.com.fourkitchen.ms_usuarios.repository.UsuarioRepository;
import br.com.fourkitchen.ms_usuarios.validation.UsuarioRegex;
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

    public UsuarioResponse atualizarUsuario(Integer id, AtualizarUsuarioRequest request) {
        Usuario usuario = buscarUsuarioPorId(id);

        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new BaseException(ErrorEnum.EMAIL_JA_CADASTRADO);
        }

        validarVinculoMesa(request.perfilUsuario(), request.idMesa());

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setPerfilUsuario(request.perfilUsuario());
        usuario.setIdMesa(request.idMesa());

        if (senhaInformada(request.senha())) {
            validarSenha(request.senha());
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return usuarioResponseMapper.map(usuarioSalvo);
    }

    public void inativarUsuario(Integer id, Integer idUsuarioAutenticado) {
        if (id.equals(idUsuarioAutenticado)) {
            throw new BaseException(ErrorEnum.NAO_PODE_EXCLUIR_PROPRIO_USUARIO);
        }

        Usuario usuario = buscarUsuarioPorId(id);

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new BaseException(ErrorEnum.USUARIO_JA_INATIVO);
        }

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    private void validarVinculoMesa(CriarUsuarioRequest request) {
        validarVinculoMesa(request.perfilUsuario(), request.idMesa());
    }

    private void validarVinculoMesa(PerfilUsuario perfilUsuario, Integer idMesa) {
        if (PerfilUsuario.MESA.equals(perfilUsuario)) {
            if (idMesa == null || idMesa <= 0) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            return;
        }

        if (idMesa != null) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
    }

    private Usuario buscarUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorEnum.USUARIO_NAO_ENCONTRADO));
    }

    private boolean senhaInformada(String senha) {
        return senha != null && !senha.isBlank();
    }

    private void validarSenha(String senha) {
        if (!senha.matches(UsuarioRegex.SENHA_FORTE)) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
    }

}
