package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GarcomChamadaService {

    private final NotificacaoService notificacaoService;

    private final GarcomMesaService garcomMesaService;

    public NotificacaoResponse concluirChamada(Integer idNotificacao, Authentication authentication) {
        extrairIdGarcom(authentication);

        boolean chamadaPertenceAsMesasAtuais = garcomMesaService.listarMesas(authentication)
                .stream()
                .flatMap(mesa -> mesa.chamadasPendentes().stream())
                .anyMatch(chamada -> Objects.equals(chamada.id(), idNotificacao));

        if (!chamadaPertenceAsMesasAtuais) {
            throw new BaseException(ErrorEnum.CHAMADA_GARCOM_NAO_PERTENCE_AO_GARCOM);
        }

        return notificacaoService.marcarComoLida(idNotificacao);
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
}
