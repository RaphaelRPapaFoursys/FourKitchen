package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidosCanalClientResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ProblemasCozinhaMotivoClientResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.RankingProdutosClientResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.VolumePedidosHorarioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.PeriodoDashboard;
import br.com.fourkitchen.bff_restaurante.dto.FiltroDashboard;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidosCanalResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ProblemasCozinhaMotivoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.RankingProdutosResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.VolumePedidosHorarioResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GestorDashboardService {

    private static final Set<String> CANAIS = Set.of("TOTEM", "MESA", "GARCOM");
    private static final Set<String> STATUS = Set.of(
            "ENVIADO_COZINHA", "EM_PREPARO", "PRONTO", "ENTREGUE", "FINALIZADO",
            "CANCELADO", "PROBLEMA_COZINHA", "AGUARDANDO_DECISAO"
    );

    private final PedidoClient pedidoClient;

    public VolumePedidosHorarioResponse buscarPedidosPorHorario(FiltroDashboard filtro) {
        validar(filtro);
        try {
            VolumePedidosHorarioClientResponse response = pedidoClient.buscarPedidosPorHorario(
                    filtro.periodo().name(), data(filtro.dataInicial()), data(filtro.dataFinal()),
                    filtro.canal(), filtro.idMesa(), filtro.status());
            return new VolumePedidosHorarioResponse(
                    response.periodo(),
                    response.totalPedidos(),
                    response.horarioPico(),
                    response.quantidadeNoPico(),
                    response.dados().stream()
                            .map(item -> new VolumePedidosHorarioResponse.Item(item.horario(), item.quantidade()))
                            .toList()
            );
        } catch (FeignException e) {
            throw indisponivel();
        }
    }

    public ProblemasCozinhaMotivoResponse buscarProblemasPorMotivo(FiltroDashboard filtro) {
        validar(filtro);
        try {
            ProblemasCozinhaMotivoClientResponse response = pedidoClient.buscarProblemasPorMotivo(
                    filtro.periodo().name(), data(filtro.dataInicial()), data(filtro.dataFinal()),
                    filtro.canal(), filtro.idMesa(), filtro.status());
            return new ProblemasCozinhaMotivoResponse(
                    response.periodo(),
                    response.totalProblemas(),
                    response.motivoMaisFrequente(),
                    response.dados().stream()
                            .map(item -> new ProblemasCozinhaMotivoResponse.Item(
                                    item.motivo(), item.descricao(), item.quantidade(), item.percentual()))
                            .toList()
            );
        } catch (FeignException e) {
            throw indisponivel();
        }
    }

    public PedidosCanalResponse buscarPedidosPorCanal(FiltroDashboard filtro) {
        validar(filtro);
        try {
            PedidosCanalClientResponse response = pedidoClient.buscarPedidosPorCanal(
                    filtro.periodo().name(), data(filtro.dataInicial()), data(filtro.dataFinal()),
                    filtro.canal(), filtro.idMesa(), filtro.status());
            return new PedidosCanalResponse(
                    response.periodo(),
                    response.totalPedidos(),
                    response.dados().stream()
                            .map(item -> new PedidosCanalResponse.Item(
                                    item.canal(), item.descricao(), item.quantidade(), item.percentual()))
                            .toList()
            );
        } catch (FeignException e) {
            throw indisponivel();
        }
    }

    public RankingProdutosResponse buscarRankingProdutos(PeriodoDashboard periodo) {
        if (periodo != PeriodoDashboard.HOJE
                && periodo != PeriodoDashboard.ULTIMOS_7_DIAS
                && periodo != PeriodoDashboard.ULTIMOS_30_DIAS) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
        try {
            RankingProdutosClientResponse response = pedidoClient.buscarRankingProdutos(periodo.name());
            return new RankingProdutosResponse(
                    response.periodo(),
                    response.dados().stream()
                            .map(item -> new RankingProdutosResponse.Item(
                                    item.idProduto(), item.nomeProduto(), item.quantidade()))
                            .toList()
            );
        } catch (FeignException e) {
            throw indisponivel();
        }
    }

    private BaseException indisponivel() {
        return new BaseException(ErrorEnum.DADOS_DASHBOARD_INDISPONIVEIS);
    }

    private void validar(FiltroDashboard filtro) {
        boolean personalizadoInvalido = filtro.periodo() == PeriodoDashboard.PERSONALIZADO
                && (filtro.dataInicial() == null || filtro.dataFinal() == null
                || filtro.dataInicial().isAfter(filtro.dataFinal())
                || filtro.dataInicial().plusYears(1).isBefore(filtro.dataFinal()));
        if (personalizadoInvalido
                || filtro.canal() != null && !CANAIS.contains(filtro.canal())
                || filtro.status() != null && !STATUS.contains(filtro.status())) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
    }

    private String data(LocalDate data) {
        return data == null ? null : data.toString();
    }
}
