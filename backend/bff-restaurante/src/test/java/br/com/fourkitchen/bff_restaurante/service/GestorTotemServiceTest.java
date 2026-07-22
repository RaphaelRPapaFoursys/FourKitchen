package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ResumoTotemClientResponse;
import br.com.fourkitchen.bff_restaurante.client.usuarios.UsuarioClient;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.TotemGestorResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorTotemServiceTest {

    private static final String AUTHORIZATION = "Bearer token";

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private PedidoClient pedidoClient;

    @InjectMocks
    private GestorTotemService service;

    @Test
    void deveCombinarSomenteUsuariosTotemComResumoDePedidos() {
        LocalDateTime ultimaAtividade = LocalDateTime.of(2026, 7, 22, 14, 30);
        when(pedidoClient.buscarResumoTotens()).thenReturn(List.of(
                new ResumoTotemClientResponse(9, 7L, new BigDecimal("245.50"), ultimaAtividade, 1L)
        ));
        when(usuarioClient.listarUsuariosAtivos(AUTHORIZATION)).thenReturn(List.of(
                new UsuarioClientResponse(3, "Garcom", "garcom@fourkitchen.com", "GARCOM", null, true),
                new UsuarioClientResponse(9, "Totem 01", "totem01@fourkitchen.com", "TOTEM", null, true),
                new UsuarioClientResponse(10, "Totem 02", "totem02@fourkitchen.com", "TOTEM", null, true)
        ));

        List<TotemGestorResponse> resultado = service.listarTotens(AUTHORIZATION);

        assertEquals(2, resultado.size());
        assertEquals("Totem 01", resultado.getFirst().nome());
        assertEquals(7L, resultado.getFirst().pedidosHoje());
        assertEquals(new BigDecimal("245.50"), resultado.getFirst().valorHoje());
        assertEquals(1L, resultado.getFirst().problemasAbertos());
        assertEquals(0L, resultado.get(1).pedidosHoje());
        assertEquals(BigDecimal.ZERO, resultado.get(1).valorHoje());
    }

    @Test
    void deveRejeitarAuthorizationInvalidoAntesDeConsultarServicos() {
        BaseException exception = assertThrows(BaseException.class, () -> service.listarTotens("token"));

        assertEquals(ErrorEnum.TOKEN_INVALIDO, exception.getErrorEnum());
        verifyNoInteractions(usuarioClient, pedidoClient);
    }
}
