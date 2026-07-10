package br.com.fourkitchen.ms_pagamentos.service;

import br.com.fourkitchen.ms_pagamentos.dto.response.PagamentoResponse;
import br.com.fourkitchen.ms_pagamentos.enums.StatusPagamento;
import br.com.fourkitchen.ms_pagamentos.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PagamentoServiceTest {
    @Mock
    private Random random;

    @InjectMocks
    private PagamentoService service;

    @Test
    void deveAprovarPagamento() {

        when(random.nextInt(10)).thenReturn(1); // 1 + 1 = 2 (par)

        PagamentoResponse response = service.pagar();

        assertEquals(StatusPagamento.APROVADO, response.status());
        assertEquals(
                StatusPagamento.APROVADO.getErrorMessage(),
                response.mensagem()
        );
        assertNotNull(response.codigoAutorizacao());
    }

    @Test
    void deveRecusarPagamento() {

        when(random.nextInt(10)).thenReturn(0); // 0 + 1 = 1 (ímpar)

        BaseException exception = assertThrows(
                BaseException.class,
                () -> service.pagar()
        );

        assertEquals(
                StatusPagamento.RECUSADO,
                exception.getErrorEnum()
        );
    }
}
