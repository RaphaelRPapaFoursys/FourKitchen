package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.AtribuirGarcomClientRequest;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaClientResponse;
import br.com.fourkitchen.bff_restaurante.client.usuarios.UsuarioClient;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.AtribuirGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.GarcomResumoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGestorResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.mapper.GarcomResumoResponseMapper;
import br.com.fourkitchen.bff_restaurante.mapper.MesaGestorMapperSource;
import br.com.fourkitchen.bff_restaurante.mapper.MesaGestorResponseMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GestorMesaService {

    private static final String PERFIL_GARCOM = "GARCOM";

    private final MesaClient mesaClient;

    private final UsuarioClient usuarioClient;

    private final MesaGestorResponseMapper mesaGestorResponseMapper;

    private final GarcomResumoResponseMapper garcomResumoResponseMapper;

    public List<MesaGestorResponse> listarMesas(String authorization) {
        List<MesaClientResponse> mesas = buscarMesas();
        Map<Integer, GarcomResumoResponse> garconsPorId = buscarGarconsPorIdQuandoNecessario(authorization, mesas);

        return mesas.stream()
                .sorted(Comparator.comparing(MesaClientResponse::numero))
                .map(mesa -> mapearMesa(mesa, garconsPorId))
                .toList();
    }

    public List<GarcomResumoResponse> listarGarcons(String authorization) {
        return buscarGarcons(authorization);
    }

    public MesaGestorResponse abrirMesa(Integer id, String authorization) {
        MesaClientResponse mesa = executarAlteracaoMesa(() -> mesaClient.abrirMesa(id));
        return mapearMesa(mesa, buscarGarconsPorIdQuandoNecessario(authorization, List.of(mesa)));
    }

    public MesaGestorResponse fecharMesa(Integer id, String authorization) {
        MesaClientResponse mesa = executarAlteracaoMesa(() -> mesaClient.fecharMesa(id));
        return mapearMesa(mesa, buscarGarconsPorIdQuandoNecessario(authorization, List.of(mesa)));
    }

    public MesaGestorResponse atribuirGarcom(Integer id, AtribuirGarcomRequest request, String authorization) {
        GarcomResumoResponse garcom = buscarGarcomPorId(request.garcomId(), authorization);
        MesaClientResponse mesa = executarAlteracaoMesa(() -> mesaClient.atribuirGarcom(
                id,
                new AtribuirGarcomClientRequest(request.garcomId())
        ));

        return mesaGestorResponseMapper.map(new MesaGestorMapperSource(mesa, garcom.nome()));
    }

    private List<MesaClientResponse> buscarMesas() {
        try {
            return mesaClient.listarMesas();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private List<GarcomResumoResponse> buscarGarcons(String authorization) {
        validarAuthorization(authorization);

        try {
            return usuarioClient.listarUsuariosAtivos(authorization)
                    .stream()
                    .filter(this::isGarcomAtivo)
                    .sorted(Comparator.comparing(UsuarioClientResponse::nome))
                    .map(garcomResumoResponseMapper::map)
                    .toList();
        } catch (FeignException e) {
            if (e.status() == 401) {
                throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
            }

            if (e.status() == 403) {
                throw new BaseException(ErrorEnum.ACESSO_NEGADO);
            }

            throw new BaseException(ErrorEnum.MS_USUARIOS_INDISPONIVEL);
        }
    }

    private Map<Integer, GarcomResumoResponse> buscarGarconsPorIdQuandoNecessario(
            String authorization,
            List<MesaClientResponse> mesas
    ) {
        boolean possuiGarcomAtribuido = mesas.stream()
                .anyMatch(mesa -> mesa.garcomId() != null);

        if (!possuiGarcomAtribuido) {
            return Map.of();
        }

        return buscarGarcons(authorization)
                .stream()
                .collect(Collectors.toMap(GarcomResumoResponse::id, Function.identity()));
    }

    private GarcomResumoResponse buscarGarcomPorId(Integer garcomId, String authorization) {
        Optional<GarcomResumoResponse> garcom = buscarGarcons(authorization)
                .stream()
                .filter(response -> response.id().equals(garcomId))
                .findFirst();

        return garcom.orElseThrow(() -> new BaseException(ErrorEnum.GARCOM_INVALIDO));
    }

    private MesaGestorResponse mapearMesa(
            MesaClientResponse mesa,
            Map<Integer, GarcomResumoResponse> garconsPorId
    ) {
        GarcomResumoResponse garcom = mesa.garcomId() == null ? null : garconsPorId.get(mesa.garcomId());

        return mesaGestorResponseMapper.map(new MesaGestorMapperSource(
                mesa,
                garcom == null ? null : garcom.nome()
        ));
    }

    private MesaClientResponse executarAlteracaoMesa(AlteracaoMesa alteracaoMesa) {
        try {
            return alteracaoMesa.executar();
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new BaseException(ErrorEnum.MESA_NAO_ENCONTRADA);
            }

            if (e.status() == 400 || e.status() == 409) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private boolean isGarcomAtivo(UsuarioClientResponse usuario) {
        return Boolean.TRUE.equals(usuario.ativo())
                && PERFIL_GARCOM.equals(usuario.perfilUsuario());
    }

    private void validarAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }
    }

    @FunctionalInterface
    private interface AlteracaoMesa {
        MesaClientResponse executar();
    }
}
