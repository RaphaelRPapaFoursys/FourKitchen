package br.com.fourkitchen.ms_usuarios.security;

import br.com.fourkitchen.ms_usuarios.dto.responseDto.LoginResponse;
import br.com.fourkitchen.ms_usuarios.enums.PerfilUsuario;
import br.com.fourkitchen.ms_usuarios.model.Usuario;
import br.com.fourkitchen.ms_usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void criarUsuario(String nome, String email, String senha) {

        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("E-mail já cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setPerfilUsuario(PerfilUsuario.ADMIN);
        usuario.setAtivo(true);

        usuarioRepository.save(usuario);
    }

    public LoginResponse login(String email, String senha) {

        Usuario usuario = usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("E-mail ou senha inválidos."));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new RuntimeException("Usuário inativo.");
        }

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new RuntimeException("E-mail ou senha inválidos.");
        }

        String token = jwtService.gerarToken(usuario);

        return new LoginResponse(
                token,
                usuario.getId().longValue(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfilUsuario().name()
        );
    }
}