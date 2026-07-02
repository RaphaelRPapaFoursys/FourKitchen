package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaGarcomClientResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.client.notificacoes.NotificacaoClient;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.mapper.MesaGarcomMapperSource;
import br.com.fourkitchen.bff_restaurante.mapper.MesaGarcomResponseMapper;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GarcomMesaService {

    private final MesaClient mesaClient;

    private final PedidoClient pedidoClient;

    private final NotificacaoClient notificacaoClient;

    private final MesaGarcomResponseMapper mesaGarcomResponseMapper;

    public List<MesaGarcomResponse> listarMesas(Authentication authentication) {
        Integer idGarcom = extrairIdGarcom(authentication);
        List<MesaGarcomClientResponse> mesas = buscarMesasDoGarcom(idGarcom);

        if (mesas.isEmpty()) {
            return List.of();
        }

        List<Integer> idsAtendimento = mesas.stream()
                .map(MesaGarcomClientResponse::idAtendimento)
                .filter(idAtendimento -> idAtendimento != null)
                .toList();

        Map<Integer, List<PedidoResponse>> pedidosPorAtendimento = buscarPedidosAtivos(idsAtendimento)
                .stream()
                .collect(Collectors.groupingBy(PedidoResponse::idAtendimento));

        Map<Integer, List<NotificacaoResponse>> chamadasPorAtendimento = buscarChamadasPendentes(idsAtendimento)
                .stream()
                .collect(Collectors.groupingBy(NotificacaoResponse::idAtendimento));

        return mesas.stream()
                .map(mesa -> mesaGarcomResponseMapper.map(new MesaGarcomMapperSource(
                        mesa,
                        pedidosPorAtendimento.getOrDefault(mesa.idAtendimento(), List.of()),
                        chamadasPorAtendimento.getOrDefault(mesa.idAtendimento(), List.of())
                )))
                .toList();
    }

    private Integer extrairIdGarcom(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuarioAutenticado)) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        if (usuarioAutenticado.id() == null) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        try {
            return Math.toIntExact(usuarioAutenticado.id());
        } catch (ArithmeticException e) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
    }

    private List<MesaGarcomClientResponse> buscarMesasDoGarcom(Integer idGarcom) {
        try {
            return mesaClient.listarMesasPorGarcom(idGarcom);
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private List<PedidoResponse> buscarPedidosAtivos(List<Integer> idsAtendimento) {
        if (idsAtendimento.isEmpty()) {
            return List.of();
        }

        try {
            return pedidoClient.listarPedidosAtivosPorAtendimentos(idsAtendimento);
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private List<NotificacaoResponse> buscarChamadasPendentes(List<Integer> idsAtendimento) {
        if (idsAtendimento.isEmpty()) {
            return List.of();
        }

        try {
            return notificacaoClient.listarChamadasPendentesPorAtendimentos(idsAtendimento);
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_NOTIFICACOES_INDISPONIVEL);
        }
    }
}
