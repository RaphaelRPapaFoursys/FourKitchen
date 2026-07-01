package br.com.fourkitchen.bff_restaurante.client.pedidos;

import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.CriarPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-pedidos", url = "${clients.ms-pedidos.url}")
public interface PedidoClient {

    @PostMapping("/pedidos")
    PedidoResponse criarPedido(@RequestBody CriarPedidoRequest request);
}
