package br.com.fourkitchen.bff_restaurante.client.pagamentos;

import br.com.fourkitchen.bff_restaurante.client.pagamentos.dto.PagamentoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "ms-pagamentos",
        url = "${clients.ms-pagamentos.url}"
)
public interface PagamentoClient {
    @PostMapping("/api/pagamentos")
    PagamentoResponse pagar();
}
