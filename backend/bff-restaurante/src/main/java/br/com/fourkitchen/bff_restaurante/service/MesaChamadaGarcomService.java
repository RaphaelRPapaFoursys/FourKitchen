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
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MesaChamadaGarcomService {

    private final MesaClient mesaClient;

    private final NotificacaoService notificacaoService;

    public NotificacaoResponse chamarGarcom(ChamarGarcomRequest request) {
        SessaoMesaResponse sessao = validarSessaoMesa(request);
        validarGarcomResponsavel(sessao);

        return notificacaoService.criarNotificacao(new CriarNotificacaoRequest(
                TipoNotificacao.CHAMADA_GARCOM,
                DestinoNotificacao.GARCOM,
                sessao.idMesa(),
                sessao.idAtendimento(),
                sessao.idGarcom()
        ));
    }

    private SessaoMesaResponse validarSessaoMesa(ChamarGarcomRequest request) {
        try {
            return mesaClient.validarSessaoMesa(request.idMesa(), request.codigoSessao());
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
