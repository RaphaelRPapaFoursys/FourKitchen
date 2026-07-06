package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.SessaoMesaResponse;
import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.TipoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.request.ChamarGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MesaChamadaGarcomService {

    private static final String PERFIL_MESA = "MESA";

    private final MesaClient mesaClient;

    private final NotificacaoService notificacaoService;

    public NotificacaoResponse chamarGarcom(ChamarGarcomRequest request, Authentication authentication) {
        UsuarioAutenticado usuario = obterUsuarioMesa(authentication);
        SessaoMesaResponse sessao = validarSessaoMesa(usuario.idMesa(), request.codigoSessao());
        validarGarcomResponsavel(sessao);

        return notificacaoService.criarNotificacao(new CriarNotificacaoRequest(
                TipoNotificacao.CHAMADA_GARCOM,
                DestinoNotificacao.GARCOM,
                sessao.idMesa(),
                sessao.idAtendimento(),
                sessao.idGarcom()
        ));
    }

    private UsuarioAutenticado obterUsuarioMesa(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        if (!PERFIL_MESA.equals(usuario.perfil()) || usuario.idMesa() == null || usuario.idMesa() <= 0) {
            throw new BaseException(ErrorEnum.ACESSO_NEGADO);
        }

        return usuario;
    }

    private SessaoMesaResponse validarSessaoMesa(Integer idMesa, Integer codigoSessao) {
        try {
            return mesaClient.validarSessaoMesa(idMesa, codigoSessao);
        } catch (FeignException e) {
            if (e.status() == 400 || e.status() == 404) {
                throw new BaseException(ErrorEnum.SESSAO_MESA_INVALIDA);
            }

            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private void validarGarcomResponsavel(SessaoMesaResponse sessao) {
        if (sessao.idGarcom() == null || sessao.idGarcom() <= 0) {
            throw new BaseException(ErrorEnum.MESA_SEM_GARCOM);
        }
    }
}
