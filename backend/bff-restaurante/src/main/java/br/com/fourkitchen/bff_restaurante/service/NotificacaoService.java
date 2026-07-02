package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.notificacoes.NotificacaoClient;
import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoClient notificacaoClient;

    public NotificacaoResponse criarNotificacao(CriarNotificacaoRequest request) {
        try {
            return notificacaoClient.criarNotificacao(request);
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            throw new BaseException(ErrorEnum.MS_NOTIFICACOES_INDISPONIVEL);
        }
    }

    public List<NotificacaoResponse> listarPendentes(DestinoNotificacao destino) {
        try {
            return notificacaoClient.listarPendentes(destino);
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            throw new BaseException(ErrorEnum.MS_NOTIFICACOES_INDISPONIVEL);
        }
    }

    public NotificacaoResponse marcarComoLida(Integer id) {
        try {
            return notificacaoClient.marcarComoLida(id);
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new BaseException(ErrorEnum.NOTIFICACAO_NAO_ENCONTRADA);
            }

            throw new BaseException(ErrorEnum.MS_NOTIFICACOES_INDISPONIVEL);
        }
    }

    public NotificacaoResponse concluirChamadaGarcom(Integer id, Integer idGarcom) {
        try {
            return notificacaoClient.concluirChamadaGarcom(id, idGarcom);
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new BaseException(ErrorEnum.NOTIFICACAO_NAO_ENCONTRADA);
            }

            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.CHAMADA_GARCOM_INVALIDA);
            }

            if (e.status() == 403) {
                throw new BaseException(ErrorEnum.CHAMADA_GARCOM_NAO_PERTENCE_AO_GARCOM);
            }

            throw new BaseException(ErrorEnum.MS_NOTIFICACOES_INDISPONIVEL);
        }
    }
}
