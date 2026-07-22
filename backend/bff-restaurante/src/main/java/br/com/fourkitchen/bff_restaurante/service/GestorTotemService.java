package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ResumoTotemClientResponse;
import br.com.fourkitchen.bff_restaurante.client.usuarios.UsuarioClient;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.TotemGestorResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GestorTotemService {

    private static final String PERFIL_TOTEM = "TOTEM";

    private final UsuarioClient usuarioClient;
    private final PedidoClient pedidoClient;

    public List<TotemGestorResponse> listarTotens(String authorization) {
        validarAuthorization(authorization);

        try {
            Map<Integer, ResumoTotemClientResponse> resumos = pedidoClient.buscarResumoTotens()
                    .stream()
                    .collect(Collectors.toMap(ResumoTotemClientResponse::idUsuario, Function.identity()));

            return usuarioClient.listarUsuariosAtivos(authorization)
                    .stream()
                    .filter(usuario -> PERFIL_TOTEM.equals(usuario.perfilUsuario()))
                    .map(usuario -> mapearTotem(usuario, resumos.get(usuario.id())))
                    .sorted(Comparator.comparing(TotemGestorResponse::nome, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        } catch (FeignException e) {
            if (e.status() == 401) throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
            if (e.status() == 403) throw new BaseException(ErrorEnum.ACESSO_NEGADO);
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private TotemGestorResponse mapearTotem(
            UsuarioClientResponse usuario,
            ResumoTotemClientResponse resumo
    ) {
        return new TotemGestorResponse(
                usuario.id(),
                usuario.nome(),
                usuario.email(),
                usuario.ativo(),
                resumo == null ? 0L : resumo.pedidosHoje(),
                resumo == null ? BigDecimal.ZERO : resumo.valorHoje(),
                resumo == null ? null : resumo.ultimaAtividade(),
                resumo == null ? 0L : resumo.problemasAbertos()
        );
    }

    private void validarAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }
    }
}
