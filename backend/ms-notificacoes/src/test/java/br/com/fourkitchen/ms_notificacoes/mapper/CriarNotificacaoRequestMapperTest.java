package br.com.fourkitchen.ms_notificacoes.mapper;

import br.com.fourkitchen.ms_notificacoes.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.ms_notificacoes.enums.DestinoNotificacao;
import br.com.fourkitchen.ms_notificacoes.enums.TipoNotificacao;
import br.com.fourkitchen.ms_notificacoes.model.Notificacao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CriarNotificacaoRequestMapperTest {

    private final CriarNotificacaoRequestMapper mapper = new CriarNotificacaoRequestMapper();

    @Test
    void mapDeveConverterRequestParaNotificacaoNaoLidaComDataAtual() {
        CriarNotificacaoRequest request = new CriarNotificacaoRequest(
                TipoNotificacao.PEDIDO_PRONTO,
                DestinoNotificacao.GARCOM
        );

        Notificacao notificacao = mapper.map(request);

        assertEquals("PEDIDO_PRONTO", notificacao.getTipo());
        assertEquals("Pedido pronto para retirada", notificacao.getMensagem());
        assertEquals(DestinoNotificacao.GARCOM, notificacao.getDestino());
        assertFalse(notificacao.getLida());
        assertNotNull(notificacao.getData());
    }
}
