package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.usuarios.UsuarioAuthClient;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioLoginRequest;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioLoginResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.LoginRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.LoginResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.UsuarioAutenticadoResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.security.JwtService;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UsuarioAuthClient usuarioAuthClient;

    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        UsuarioLoginResponse usuarioLoginResponse = autenticarNoMsUsuarios(request);

        try {
            jwtService.validarToken(usuarioLoginResponse.token());
        } catch (Exception e) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        return new LoginResponse(
                usuarioLoginResponse.token(),
                TOKEN_TYPE,
                new UsuarioAutenticadoResponse(
                        usuarioLoginResponse.id(),
                        usuarioLoginResponse.nome(),
                        usuarioLoginResponse.useremail(),
                        usuarioLoginResponse.perfil()
                )
        );
    }

    public UsuarioAutenticadoResponse me(Authentication authentication) {
        if (authentication == null
                || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuarioAutenticado)) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        return new UsuarioAutenticadoResponse(
                usuarioAutenticado.id(),
                usuarioAutenticado.nome(),
                usuarioAutenticado.email(),
                usuarioAutenticado.perfil()
        );
    }

    private UsuarioLoginResponse autenticarNoMsUsuarios(LoginRequest request) {
        try {
            return usuarioAuthClient.login(new UsuarioLoginRequest(request.email(), request.senha()));
        } catch (FeignException e) {
            if (e.status() == 400 || e.status() == 401 || e.status() == 403) {
                throw new BaseException(ErrorEnum.CREDENCIAIS_INVALIDAS);
            }

            throw new BaseException(ErrorEnum.MS_USUARIOS_INDISPONIVEL);
        }
    }
}
