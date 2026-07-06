package br.com.fourkitchen.ms_notificacoes.service;

import br.com.fourkitchen.ms_notificacoes.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.ms_notificacoes.dto.response.NotificacaoResponse;
import br.com.fourkitchen.ms_notificacoes.dto.response.ResumoNotificacoesOperacaoResponse;
import br.com.fourkitchen.ms_notificacoes.enums.DestinoNotificacao;
import br.com.fourkitchen.ms_notificacoes.enums.TipoNotificacao;
import br.com.fourkitchen.ms_notificacoes.exception.BaseException;
import br.com.fourkitchen.ms_notificacoes.exception.ErrorEnum;
import br.com.fourkitchen.ms_notificacoes.mapper.CriarNotificacaoRequestMapper;
import br.com.fourkitchen.ms_notificacoes.mapper.NotificacaoResponseMapper;
import br.com.fourkitchen.ms_notificacoes.model.Notificacao;
import br.com.fourkitchen.ms_notificacoes.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    private final CriarNotificacaoRequestMapper criarNotificacaoRequestMapper;

    private final NotificacaoResponseMapper notificacaoResponseMapper;

    public NotificacaoResponse criarNotificacao(CriarNotificacaoRequest request) {
        validarContextoDaChamadaGarcom(request);

        Notificacao notificacao = criarNotificacaoRequestMapper.map(request);

        Notificacao notificacaoSalva = notificacaoRepository.save(notificacao);

        return notificacaoResponseMapper.map(notificacaoSalva);
    }

    public List<NotificacaoResponse> listarPendentes(DestinoNotificacao destino) {
        List<Notificacao> notificacoes = destino == null
                ? notificacaoRepository.findByLidaFalseOrderByDataDesc()
                : notificacaoRepository.findByDestinoAndLidaFalseOrderByDataDesc(destino);

        return notificacoes
                .stream()
                .map(notificacaoResponseMapper::map)
                .toList();
    }

    public List<NotificacaoResponse> listarChamadasPendentesPorAtendimentos(List<Integer> idsAtendimento) {
        if (idsAtendimento == null || idsAtendimento.isEmpty()) {
            return List.of();
        }

        return notificacaoRepository
                .findByTipoAndDestinoAndLidaFalseAndIdAtendimentoInOrderByDataDesc(
                        TipoNotificacao.CHAMADA_GARCOM.name(),
                        DestinoNotificacao.GARCOM,
                        idsAtendimento
                )
                .stream()
                .map(notificacaoResponseMapper::map)
                .toList();
    }

    public ResumoNotificacoesOperacaoResponse buscarResumoOperacao() {
        return new ResumoNotificacoesOperacaoResponse(
                notificacaoRepository.countByTipoAndDestinoAndLidaFalse(
                        TipoNotificacao.CHAMADA_GARCOM.name(),
                        DestinoNotificacao.GARCOM
                )
        );
    }

    public NotificacaoResponse marcarComoLida(Integer id) {
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorEnum.NOTIFICACAO_NAO_ENCONTRADA));

        notificacao.setLida(true);

        Notificacao notificacaoSalva = notificacaoRepository.save(notificacao);

        return notificacaoResponseMapper.map(notificacaoSalva);
    }

    public NotificacaoResponse concluirChamadaGarcom(Integer id, Integer idGarcom) {
        if (idGarcom == null || idGarcom <= 0) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }

        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorEnum.NOTIFICACAO_NAO_ENCONTRADA));

        validarChamadaGarcomPendente(notificacao);

        if (!Objects.equals(notificacao.getIdGarcom(), idGarcom)) {
            throw new BaseException(ErrorEnum.CHAMADA_GARCOM_NAO_PERTENCE_AO_GARCOM);
        }

        notificacao.setLida(true);

        Notificacao notificacaoSalva = notificacaoRepository.save(notificacao);

        return notificacaoResponseMapper.map(notificacaoSalva);
    }

    private void validarContextoDaChamadaGarcom(CriarNotificacaoRequest request) {
        if (request.tipo() != TipoNotificacao.CHAMADA_GARCOM) {
            return;
        }

        if (request.destino() != DestinoNotificacao.GARCOM
                || request.idAtendimento() == null
                || request.idGarcom() == null) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
    }

    private void validarChamadaGarcomPendente(Notificacao notificacao) {
        boolean chamadaGarcomValida = TipoNotificacao.CHAMADA_GARCOM.name().equals(notificacao.getTipo())
                && notificacao.getDestino() == DestinoNotificacao.GARCOM
                && Boolean.FALSE.equals(notificacao.getLida())
                && notificacao.getIdAtendimento() != null
                && notificacao.getIdGarcom() != null;

        if (!chamadaGarcomValida) {
            throw new BaseException(ErrorEnum.CHAMADA_GARCOM_INVALIDA);
        }
    }
}
