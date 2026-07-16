package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.response.ChamadaPendenteMesaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.realtime.RealtimeNotifier;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GarcomChamadaServiceTest {

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private GarcomMesaService garcomMesaService;

    @Mock
    private RealtimeNotifier realtimeNotifier;

    @InjectMocks
    private GarcomChamadaService garcomChamadaService;

    @Test
    void concluirChamadaDeveUsarGarcomAutenticado() {
        Authentication authentication = criarAuthentication(7L);
        NotificacaoResponse response = criarResponse();

        when(garcomMesaService.listarMesas(authentication)).thenReturn(List.of(criarMesaComChamada()));
        when(notificacaoService.marcarComoLida(3)).thenReturn(response);

        NotificacaoResponse resultado = garcomChamadaService.concluirChamada(3, authentication);

        assertSame(response, resultado);
        verify(notificacaoService).marcarComoLida(3);
    }

    @Test
    void concluirChamadaDeveRecusarChamadaQueNaoEstaNasMesasDoGarcom() {
        Authentication authentication = criarAuthentication(7L);
        when(garcomMesaService.listarMesas(authentication)).thenReturn(List.of());

        BaseException exception = assertThrows(
                BaseException.class,
                () -> garcomChamadaService.concluirChamada(3, authentication)
        );

        assertEquals(ErrorEnum.CHAMADA_GARCOM_NAO_PERTENCE_AO_GARCOM, exception.getErrorEnum());
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void concluirChamadaDeveLancarTokenInvalidoQuandoAuthenticationForNulo() {
        BaseException exception = assertThrows(BaseException.class, () -> garcomChamadaService.concluirChamada(3, null));

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void concluirChamadaDeveLancarTokenInvalidoQuandoPrincipalNaoForUsuarioAutenticado() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("garcom", null, List.of());

        BaseException exception = assertThrows(BaseException.class, () -> garcomChamadaService.concluirChamada(3, authentication));

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void concluirChamadaDeveLancarTokenInvalidoQuandoIdGarcomForNulo() {
        Authentication authentication = criarAuthentication(null);

        BaseException exception = assertThrows(BaseException.class, () -> garcomChamadaService.concluirChamada(3, authentication));

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
        verifyNoInteractions(notificacaoService);
    }

    @Test
    void concluirChamadaDeveLancarDadosInvalidosQuandoIdGarcomNaoCouberEmInteger() {
        Authentication authentication = criarAuthentication((long) Integer.MAX_VALUE + 1);

        BaseException exception = assertThrows(BaseException.class, () -> garcomChamadaService.concluirChamada(3, authentication));

        assertEquals(ErrorEnum.DADOS_INVALIDOS, exception.getErrorEnum());
        verifyNoInteractions(notificacaoService);
    }

    private Authentication criarAuthentication(Long idGarcom) {
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                idGarcom,
                "Amanda",
                "amanda@fourkitchen.com",
                "GARCOM",
                null
        );

        return new UsernamePasswordAuthenticationToken(usuario, null, List.of());
    }

    private NotificacaoResponse criarResponse() {
        return new NotificacaoResponse(
                3,
                "CHAMADA_GARCOM",
                "Cliente solicitou atendimento",
                DestinoNotificacao.GARCOM,
                true,
                LocalDateTime.of(2026, 7, 2, 10, 15),
                1,
                8,
                7
        );
    }

    private MesaGarcomResponse criarMesaComChamada() {
        return new MesaGarcomResponse(
                1,
                10,
                "OCUPADA",
                8,
                123456,
                7,
                LocalDateTime.of(2026, 7, 2, 10, 0),
                List.of(),
                List.of(new ChamadaPendenteMesaResponse(
                        3,
                        "CHAMADA_GARCOM",
                        "Cliente solicitou atendimento",
                        LocalDateTime.of(2026, 7, 2, 10, 15)
                )),
                true
        );
    }
}
