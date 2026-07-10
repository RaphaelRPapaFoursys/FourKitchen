package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.usuarios.UsuarioClient;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.AtualizarUsuarioClientRequest;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.CriarUsuarioClientRequest;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarUsuarioRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarUsuarioRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.UsuarioGestorResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GestorUsuarioService {

    private final UsuarioClient usuarioClient;

    public List<UsuarioGestorResponse> listarUsuarios(String authorization) {
        validarAuthorization(authorization);

        try {
            return usuarioClient.listarUsuariosAtivos(authorization)
                    .stream()
                    .sorted(Comparator.comparing(UsuarioClientResponse::nome))
                    .map(this::mapearUsuario)
                    .toList();
        } catch (FeignException e) {
            throw mapearErroMsUsuarios(e);
        }
    }

    public UsuarioGestorResponse criarUsuario(CriarUsuarioRequest request, String authorization) {
        validarAuthorization(authorization);

        CriarUsuarioClientRequest clientRequest = new CriarUsuarioClientRequest(
                request.nome(),
                request.email(),
                request.senha(),
                request.perfilUsuario(),
                request.idMesa()
        );

        try {
            return mapearUsuario(usuarioClient.criarUsuario(clientRequest, authorization));
        } catch (FeignException e) {
            throw mapearErroMsUsuarios(e);
        }
    }

    public UsuarioGestorResponse atualizarUsuario(
            Integer id,
            AtualizarUsuarioRequest request,
            String authorization
    ) {
        validarAuthorization(authorization);

        AtualizarUsuarioClientRequest clientRequest = new AtualizarUsuarioClientRequest(
                request.nome(),
                request.email(),
                request.senha(),
                request.perfilUsuario(),
                request.idMesa()
        );

        try {
            return mapearUsuario(usuarioClient.atualizarUsuario(id, clientRequest, authorization));
        } catch (FeignException e) {
            throw mapearErroMsUsuarios(e);
        }
    }

    public void inativarUsuario(Integer id, String authorization, Authentication authentication) {
        validarAuthorization(authorization);
        UsuarioAutenticado usuarioAutenticado = obterUsuarioAutenticado(authentication);
        Integer idUsuarioAutenticado = extrairIdUsuarioAutenticado(usuarioAutenticado);

        if (id.equals(idUsuarioAutenticado)) {
            throw new BaseException(ErrorEnum.NAO_PODE_EXCLUIR_PROPRIO_USUARIO);
        }

        try {
            usuarioClient.inativarUsuario(id, authorization);
        } catch (FeignException e) {
            throw mapearErroMsUsuarios(e);
        }
    }

    private UsuarioGestorResponse mapearUsuario(UsuarioClientResponse usuario) {
        return new UsuarioGestorResponse(
                usuario.id(),
                usuario.nome(),
                usuario.email(),
                usuario.perfilUsuario(),
                usuario.idMesa(),
                usuario.ativo()
        );
    }

    private UsuarioAutenticado obterUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuarioAutenticado)) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        return usuarioAutenticado;
    }

    private Integer extrairIdUsuarioAutenticado(UsuarioAutenticado usuarioAutenticado) {
        if (usuarioAutenticado.id() == null) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        try {
            return Math.toIntExact(usuarioAutenticado.id());
        } catch (ArithmeticException e) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
    }

    private void validarAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }
    }

    private BaseException mapearErroMsUsuarios(FeignException e) {
        return switch (e.status()) {
            case 400 -> new BaseException(ErrorEnum.DADOS_INVALIDOS);
            case 401 -> new BaseException(ErrorEnum.TOKEN_INVALIDO);
            case 403 -> new BaseException(ErrorEnum.ACESSO_NEGADO);
            case 404 -> new BaseException(ErrorEnum.USUARIO_NAO_ENCONTRADO);
            case 409 -> new BaseException(ErrorEnum.EMAIL_JA_CADASTRADO);
            default -> new BaseException(ErrorEnum.MS_USUARIOS_INDISPONIVEL);
        };
    }
}
