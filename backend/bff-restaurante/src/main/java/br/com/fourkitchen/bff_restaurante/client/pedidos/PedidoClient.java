package br.com.fourkitchen.bff_restaurante.client.pedidos;

import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.*;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "ms-pedidos", url = "${clients.ms-pedidos.url}")
public interface PedidoClient {

    @PostMapping("/pedidos")
    PedidoResponse criarPedido(@RequestBody CriarPedidoRequest request);

    @GetMapping("/pedidos/cozinha/fila")
    List<PedidoCozinhaResponse> listarFilaCozinha();

    @PatchMapping("/pedidos/{id}/iniciar-preparo")
    PedidoResponse iniciarPreparo(@PathVariable Integer id);

    @PatchMapping("/pedidos/{id}/finalizar-preparo")
    PedidoResponse finalizarPreparo(@PathVariable Integer id);

    @GetMapping("/pedidos/atendimentos/ativos")
    List<PedidoResponse> listarPedidosAtivosPorAtendimentos(
            @RequestParam("idsAtendimento") List<Integer> idsAtendimento
    );

    @GetMapping("/pedidos/atendimentos/ativos/detalhado")
    List<PedidoCozinhaResponse> listarPedidosAtivosDetalhadosPorAtendimentos(
            @RequestParam("idsAtendimento") List<Integer> idsAtendimento
    );

    @PatchMapping("/pedidos/{id}/entregar")
    PedidoResponse entregarPedido(@PathVariable Integer id);
    @PatchMapping("/pedidos/sinalizar-problema")
    SinalizarProblemaResponse sinalizarProblema (
            @RequestBody SinalizarProblemaRequest request);

}
