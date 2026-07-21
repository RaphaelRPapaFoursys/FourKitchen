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

    @PostMapping("/api/pedidos")
    PedidoResponse criarPedido(@RequestBody CriarPedidoRequest request);

    @GetMapping("/api/pedidos/cozinha/fila")
    List<PedidoCozinhaResponse> listarFilaCozinha();

    @GetMapping("/api/pedidos/totem/problemas")
    List<PedidoProblemaTotemResponse> listarProblemasTotem();

    @GetMapping("/api/pedidos/totem/fila-retirada")
    List<PedidoRetiradaResponse> listarFilaRetiradaTotem();

    @PatchMapping("/api/pedidos/{id}/iniciar-preparo")
    PedidoResponse iniciarPreparo(@PathVariable Integer id);

    @PatchMapping("/api/pedidos/{id}/finalizar-preparo")
    PedidoResponse finalizarPreparo(@PathVariable Integer id);

    @GetMapping("/api/pedidos/atendimentos/ativos")
    List<PedidoResponse> listarPedidosAtivosPorAtendimentos(
            @RequestParam("idsAtendimento") List<Integer> idsAtendimento
    );

    @GetMapping("/api/pedidos/resumo-operacao")
    ResumoPedidosOperacaoResponse buscarResumoOperacao();

    @GetMapping("/api/pedidos/dashboard/pedidos-por-horario")
    VolumePedidosHorarioClientResponse buscarPedidosPorHorario(
            @RequestParam("periodo") String periodo,
            @RequestParam(value = "dataInicial", required = false) String dataInicial,
            @RequestParam(value = "dataFinal", required = false) String dataFinal,
            @RequestParam(value = "canal", required = false) String canal,
            @RequestParam(value = "idMesa", required = false) Integer idMesa,
            @RequestParam(value = "status", required = false) String status
    );

    @GetMapping("/api/pedidos/dashboard/problemas-por-motivo")
    ProblemasCozinhaMotivoClientResponse buscarProblemasPorMotivo(
            @RequestParam("periodo") String periodo,
            @RequestParam(value = "dataInicial", required = false) String dataInicial,
            @RequestParam(value = "dataFinal", required = false) String dataFinal,
            @RequestParam(value = "canal", required = false) String canal,
            @RequestParam(value = "idMesa", required = false) Integer idMesa,
            @RequestParam(value = "status", required = false) String status
    );

    @GetMapping("/api/pedidos/dashboard/pedidos-por-canal")
    PedidosCanalClientResponse buscarPedidosPorCanal(
            @RequestParam("periodo") String periodo,
            @RequestParam(value = "dataInicial", required = false) String dataInicial,
            @RequestParam(value = "dataFinal", required = false) String dataFinal,
            @RequestParam(value = "canal", required = false) String canal,
            @RequestParam(value = "idMesa", required = false) Integer idMesa,
            @RequestParam(value = "status", required = false) String status
    );

    @GetMapping("/api/pedidos/dashboard/ranking-produtos")
    RankingProdutosClientResponse buscarRankingProdutos(@RequestParam("periodo") String periodo);

    @GetMapping("/api/pedidos/atendimentos/ativos/detalhado")
    List<PedidoCozinhaResponse> listarPedidosAtivosDetalhadosPorAtendimentos(
            @RequestParam("idsAtendimento") List<Integer> idsAtendimento
    );

    @GetMapping("/api/pedidos/atendimentos/{atendimentoId}/detalhado")
    List<PedidoCozinhaResponse> listarPedidosDetalhadosPorAtendimento(
            @PathVariable Integer atendimentoId
    );

    @GetMapping("/api/pedidos/atendimentos/{atendimentoId}/resumo-conta")
    ResumoContaAtendimentoResponse buscarResumoContaAtendimento(
            @PathVariable Integer atendimentoId
    );

    @PatchMapping("/api/pedidos/{id}/entregar")
    PedidoResponse entregarPedido(@PathVariable Integer id);

    @PatchMapping("/api/pedidos/totem/{id}/entregar")
    PedidoResponse entregarPedidoTotem(@PathVariable Integer id);

    @PatchMapping("/api/pedidos/{id}/cancelar")
    Void cancelarPedidoAntesDoPreparo(@PathVariable Integer id);

    @PatchMapping("/api/pedidos/sinalizar-problema")
    SinalizarProblemaResponse sinalizarProblema(@RequestBody SinalizarProblemaRequest request);

    @PatchMapping("/api/pedidos/decisao-problema")
    Void decisaoProblema(
            @RequestBody DecisaoProblemaPedidoRequest decisaoProblemaRequest
    );

    @PatchMapping("/api/pedidos/{id}/problemas-totem/assumir")
    Void assumirProblemaTotem(
            @PathVariable Integer id,
            @RequestBody AssumirProblemaTotemRequest request
    );

    @PatchMapping("/api/pedidos/problemas-totem/decisao")
    Void decisaoProblemaTotem(
            @RequestParam("idGarcom") Integer idGarcom,
            @RequestBody DecisaoProblemaPedidoRequest request
    );
}
