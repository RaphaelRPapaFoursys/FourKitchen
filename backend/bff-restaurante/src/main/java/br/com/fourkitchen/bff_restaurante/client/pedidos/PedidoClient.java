package br.com.fourkitchen.bff_restaurante.client.pedidos;

import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.AlterarPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.CriarPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "ms-pedidos", url = "${clients.ms-pedidos.url}")
public interface PedidoClient {

    @PostMapping("/pedidos")
    PedidoResponse criarPedido(@RequestBody CriarPedidoRequest request);

    @GetMapping("/pedidos/cozinha/fila")
    List<PedidoCozinhaResponse> listarFilaCozinha();

    @PatchMapping("/pedidos/{id}")
    void alterarPedido(@PathVariable Integer id, @RequestBody AlterarPedidoRequest request);
}
