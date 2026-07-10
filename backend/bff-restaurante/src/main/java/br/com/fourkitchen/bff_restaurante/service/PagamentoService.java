package br.com.fourkitchen.bff_restaurante.service;


import br.com.fourkitchen.bff_restaurante.client.pagamentos.PagamentoClient;
import br.com.fourkitchen.bff_restaurante.client.pagamentos.dto.PagamentoResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PagamentoService {
    private final PagamentoClient pagamentoClient;

    public PagamentoResponse pagar() {
        try {
            return pagamentoClient.pagar();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.PAGAMENTO_RECUSADO);
        }
    }

}
