package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.SessaoMesaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaAtendimentoAtualResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MesaAtendimentoServiceTest {

    @Mock
    private MesaClient mesaClient;

    @InjectMocks
    private MesaAtendimentoService mesaAtendimentoService;

    @Test
    void buscarAtendimentoAtualDeveRetornarDadosDaMesaAutenticada() {
        SessaoMesaResponse sessao = new SessaoMesaResponse(1, 8, 123456, 7, "OCUPADA");

        when(mesaClient.buscarAtendimentoAtual(1)).thenReturn(sessao);

        MesaAtendimentoAtualResponse response =
                mesaAtendimentoService.buscarAtendimentoAtual(criarAuthenticationMesa());

        assertEquals(1, response.idMesa());
        assertEquals(8, response.idAtendimento());
        assertEquals(123456, response.codigoAtendimento());
        assertEquals("OCUPADA", response.status());
        verify(mesaClient).buscarAtendimentoAtual(1);
    }

    @Test
    void buscarAtendimentoAtualDeveBloquearUsuarioMesaSemVinculo() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                101L,
                "Mesa sem vinculo",
                "mesa@fourkitchen.com",
                "MESA",
                null
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, List.of());

        BaseException exception = assertThrows(
                BaseException.class,
                () -> mesaAtendimentoService.buscarAtendimentoAtual(authentication)
        );

        assertEquals(ErrorEnum.ACESSO_NEGADO, exception.getErrorEnum());
        verifyNoInteractions(mesaClient);
    }

    @Test
    void buscarAtendimentoAtualDeveMapearAtendimentoNaoAberto() {
        when(mesaClient.buscarAtendimentoAtual(1)).thenThrow(feignException(400));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> mesaAtendimentoService.buscarAtendimentoAtual(criarAuthenticationMesa())
        );

        assertEquals(ErrorEnum.ATENDIMENTO_NAO_ABERTO, exception.getErrorEnum());
        verify(mesaClient).buscarAtendimentoAtual(1);
    }

    private Authentication criarAuthenticationMesa() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(
                101L,
                "Mesa 1",
                "mesa01@fourkitchen.com",
                "MESA",
                1
        );

        return new UsernamePasswordAuthenticationToken(usuario, null, List.of());
    }

    private FeignException feignException(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/api/mesas/1/atendimento-atual",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null
        );

        Response response = Response.builder()
                .status(status)
                .reason("Erro")
                .request(request)
                .build();

        return FeignException.errorStatus("buscarAtendimentoAtual", response);
    }
}
