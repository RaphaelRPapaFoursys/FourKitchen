package br.com.fourkitchen.ms_usuarios.security;

package br.com.fourkitchen.ms_usuarios.security;

import br.com.fourkitchen.ms_usuarios.dto.responseDto.LoginResponse;
import br.com.fourkitchen.ms_usuarios.entity.Usuario;
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

    public void criarUsuario(String username, String password) {

        if (usuarioRepository.existsByUsername(username)) {
            throw new RuntimeException("Usuário já cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNome(username);
        usuario.setPerfil("CLIENTE");
        usuario.setAtivo(true);

        usuarioRepository.save(usuario);
    }

    public LoginResponse login(String username, String password) {

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário ou senha inválidos."));

        if (!usuario.getAtivo()) {
            throw new RuntimeException("Usuário inativo.");
        }

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Usuário ou senha inválidos.");
        }

        String token = jwtService.gerarToken(usuario);

        return new LoginResponse(
                token,
                usuario.getId(),
                usuario.getNome(),
                usuario.getUsername(),
                usuario.getPerfil()
        );
    }
}
