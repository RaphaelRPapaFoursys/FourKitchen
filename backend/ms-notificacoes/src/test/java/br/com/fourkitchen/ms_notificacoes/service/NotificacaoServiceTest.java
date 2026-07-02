package br.com.fourkitchen.ms_notificacoes.service;

import br.com.fourkitchen.ms_notificacoes.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.ms_notificacoes.dto.response.NotificacaoResponse;
import br.com.fourkitchen.ms_notificacoes.enums.DestinoNotificacao;
import br.com.fourkitchen.ms_notificacoes.enums.TipoNotificacao;
import br.com.fourkitchen.ms_notificacoes.exception.BaseException;
import br.com.fourkitchen.ms_notificacoes.exception.ErrorEnum;
import br.com.fourkitchen.ms_notificacoes.mapper.CriarNotificacaoRequestMapper;
import br.com.fourkitchen.ms_notificacoes.mapper.NotificacaoResponseMapper;
import br.com.fourkitchen.ms_notificacoes.model.Notificacao;
import br.com.fourkitchen.ms_notificacoes.repository.NotificacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private CriarNotificacaoRequestMapper criarNotificacaoRequestMapper;

    @Mock
    private NotificacaoResponseMapper notificacaoResponseMapper;

    @InjectMocks
    private NotificacaoService notificacaoService;

    @Test
    void criarNotificacaoDeveSalvarComoNaoLidaComDataAtual() {
        CriarNotificacaoRequest request = new CriarNotificacaoRequest(
                TipoNotificacao.PEDIDO_PRONTO,
                DestinoNotificacao.GARCOM
        );
        Notificacao notificacaoMapeada = criarNotificacao(null, false);
        Notificacao notificacaoSalva = criarNotificacao(1, false);
        NotificacaoResponse response = criarResponse(notificacaoSalva);

        when(criarNotificacaoRequestMapper.map(request)).thenReturn(notificacaoMapeada);
        when(notificacaoRepository.save(notificacaoMapeada)).thenReturn(notificacaoSalva);
        when(notificacaoResponseMapper.map(notificacaoSalva)).thenReturn(response);

        NotificacaoResponse resultado = notificacaoService.criarNotificacao(request);

        assertSame(response, resultado);
        verify(criarNotificacaoRequestMapper).map(request);
        verify(notificacaoRepository).save(notificacaoMapeada);
        verify(notificacaoResponseMapper).map(notificacaoSalva);
    }

    @Test
    void listarPendentesDeveRetornarTodasAsNotificacoesPendentesQuandoDestinoNaoForInformado() {
        Notificacao notificacao = criarNotificacao(1, false);
        NotificacaoResponse response = criarResponse(notificacao);

        when(notificacaoRepository.findByLidaFalseOrderByDataDesc()).thenReturn(List.of(notificacao));
        when(notificacaoResponseMapper.map(notificacao)).thenReturn(response);

        List<NotificacaoResponse> resultado = notificacaoService.listarPendentes(null);

        assertEquals(List.of(response), resultado);
        verify(notificacaoRepository).findByLidaFalseOrderByDataDesc();
        verify(notificacaoResponseMapper).map(notificacao);
    }

    @Test
    void listarPendentesDeveFiltrarPorDestinoQuandoDestinoForInformado() {
        Notificacao notificacao = criarNotificacao(1, false);
        NotificacaoResponse response = criarResponse(notificacao);

        when(notificacaoRepository.findByDestinoAndLidaFalseOrderByDataDesc(DestinoNotificacao.COZINHA))
                .thenReturn(List.of(notificacao));
        when(notificacaoResponseMapper.map(notificacao)).thenReturn(response);

        List<NotificacaoResponse> resultado = notificacaoService.listarPendentes(DestinoNotificacao.COZINHA);

        assertEquals(List.of(response), resultado);
        verify(notificacaoRepository).findByDestinoAndLidaFalseOrderByDataDesc(DestinoNotificacao.COZINHA);
        verify(notificacaoResponseMapper).map(notificacao);
    }

    @Test
    void marcarComoLidaDeveSalvarNotificacaoLida() {
        Notificacao notificacao = criarNotificacao(1, false);
        Notificacao notificacaoSalva = criarNotificacao(1, true);
        NotificacaoResponse response = criarResponse(notificacaoSalva);

        when(notificacaoRepository.findById(1)).thenReturn(Optional.of(notificacao));
        when(notificacaoRepository.save(notificacao)).thenReturn(notificacaoSalva);
        when(notificacaoResponseMapper.map(notificacaoSalva)).thenReturn(response);

        NotificacaoResponse resultado = notificacaoService.marcarComoLida(1);

        assertSame(response, resultado);
        assertTrue(notificacao.getLida());
        verify(notificacaoRepository).findById(1);
        verify(notificacaoRepository).save(notificacao);
        verify(notificacaoResponseMapper).map(notificacaoSalva);
    }

    @Test
    void marcarComoLidaDeveLancarExcecaoQuandoNotificacaoNaoExistir() {
        when(notificacaoRepository.findById(99)).thenReturn(Optional.empty());

        BaseException exception = assertThrows(BaseException.class, () -> notificacaoService.marcarComoLida(99));

        assertEquals(ErrorEnum.NOTIFICACAO_NAO_ENCONTRADA, exception.getErrorEnum());
        verify(notificacaoRepository).findById(99);
        verify(notificacaoRepository, never()).save(any());
        verifyNoInteractions(notificacaoResponseMapper);
    }

    private Notificacao criarNotificacao(Integer id, Boolean lida) {
        return Notificacao
                .builder()
                .id(id)
                .tipo("PEDIDO_PRONTO")
                .mensagem("Pedido pronto para retirada")
                .destino(DestinoNotificacao.COZINHA)
                .lida(lida)
                .data(LocalDateTime.of(2026, 6, 30, 12, 0))
                .build();
    }

    private NotificacaoResponse criarResponse(Notificacao notificacao) {
        return new NotificacaoResponse(
                notificacao.getId(),
                notificacao.getTipo(),
                notificacao.getMensagem(),
                notificacao.getDestino(),
                notificacao.getLida(),
                notificacao.getData()
        );
    }
}
