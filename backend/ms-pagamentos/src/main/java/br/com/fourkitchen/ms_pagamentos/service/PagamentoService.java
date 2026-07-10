package br.com.fourkitchen.ms_pagamentos.service;

import br.com.fourkitchen.ms_pagamentos.dto.response.PagamentoResponse;
import br.com.fourkitchen.ms_pagamentos.exception.BaseException;
import br.com.fourkitchen.ms_pagamentos.enums.StatusPagamento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final Random random;

    public PagamentoResponse pagar(){

        int numero = random.nextInt(10) + 1;

        if (numero % 2 == 0){
            return new PagamentoResponse(StatusPagamento.APROVADO, StatusPagamento.APROVADO.getErrorMessage(), UUID.randomUUID().toString());
        }
        throw new BaseException(StatusPagamento.RECUSADO);
    }
}
