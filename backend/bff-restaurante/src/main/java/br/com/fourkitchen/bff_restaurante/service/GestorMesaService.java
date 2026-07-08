package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.AtribuirGarcomClientRequest;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.HistoricoAtendimentoClientResponse;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaClientResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.usuarios.UsuarioClient;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioClientResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.AtribuirGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.CargaGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.GarcomResumoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.HistoricoAtendimentoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGestorPaginadaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaGestorResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoGestorResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ResumoPainelResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.mapper.GarcomResumoResponseMapper;
import br.com.fourkitchen.bff_restaurante.mapper.MesaGestorMapperSource;
import br.com.fourkitchen.bff_restaurante.mapper.MesaGestorResponseMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GestorMesaService {

    private static final String PERFIL_GARCOM = "GARCOM";

    private static final String STATUS_PEDIDO_PRONTO = "PRONTO";
    private static final String STATUS_MESA_OCUPADA = "OCUPADA";
    private static final String STATUS_MESA_DISPONIVEL = "DISPONIVEL";
    private static final String STATUS_PAINEL_EM_PREPARO = "EM_PREPARO";
    private static final String STATUS_PAINEL_PRONTO_ENTREGA = "PRONTO_ENTREGA";
    private static final String STATUS_PAINEL_CONTA_ABERTA = "CONTA_ABERTA";
    private static final String CRITICIDADE_CRITICO = "critico";
    private static final List<String> STATUS_PEDIDO_EM_PREPARO = List.of(
            "ENVIADO_COZINHA",
            "EM_PREPARO",
            "AGUARDANDO_DECISAO"
    );
    private static final long LIMIAR_PREPARO_ATENCAO_MINUTOS = 14;
    private static final long LIMIAR_PRONTO_OK_MINUTOS = 5;
    private static final long LIMIAR_PRONTO_ATENCAO_MINUTOS = 10;
    private static final long CACHE_PAINEL_TTL_MILLIS = 5_000;

    private final MesaClient mesaClient;

    private final UsuarioClient usuarioClient;

    private final PedidoClient pedidoClient;

    private final MesaGestorResponseMapper mesaGestorResponseMapper;

    private final GarcomResumoResponseMapper garcomResumoResponseMapper;

    private final Object painelCacheLock = new Object();

    private volatile PainelSnapshot painelSnapshot;

    public List<MesaGestorResponse> listarMesas(String authorization) {
        List<MesaClientResponse> mesas = buscarMesas();
        Map<Integer, GarcomResumoResponse> garconsPorId = buscarGarconsPorIdQuandoNecessario(authorization, mesas);
        Map<Integer, List<PedidoGestorResponse>> pedidosPorAtendimento = buscarPedidosPorAtendimento(mesas);

        return mesas.stream()
                .sorted(Comparator.comparing(MesaClientResponse::numero))
                .map(mesa -> mapearMesa(mesa, garconsPorId, pedidosPorAtendimento))
                .toList();
    }

    // RESPOSTA MESA GESTOR
    public MesaGestorPaginadaResponse listarMesasPaginadas(
            String authorization,
            Integer page,
            Integer size,
            String sort,
            String filtroEstado,
            Integer garcomId,
            String busca
    ) {
        int paginaSolicitada = normalizarPage(page);
        int tamanhoPagina = normalizarSize(size);
        List<MesaPainelCalculo> mesas = carregarSnapshotPainel(authorization).mesas();

        List<MesaPainelCalculo> filtradas = mesas.stream()
                .filter(mesa -> aplicarFiltroEstado(mesa, filtroEstado))
                .filter(mesa -> garcomId == null || garcomId.equals(mesa.mesa().garcomId()))
                .filter(mesa -> aplicarBusca(mesa, busca))
                .sorted(comparadorMesas(sort))
                .toList();

        long totalElements = filtradas.size();
        int totalPages = calcularTotalPaginas(totalElements, tamanhoPagina);
        int inicio = Math.min(paginaSolicitada * tamanhoPagina, filtradas.size());
        int fim = Math.min(inicio + tamanhoPagina, filtradas.size());

        return new MesaGestorPaginadaResponse(
                filtradas.subList(inicio, fim).stream()
                        .map(this::mapearMesa)
                        .toList(),
                paginaSolicitada,
                tamanhoPagina,
                totalElements,
                totalPages,
                paginaSolicitada == 0,
                totalPages == 0 || paginaSolicitada >= totalPages - 1
        );
    }

    public ResumoPainelResponse buscarResumoPainel(String authorization) {
        PainelSnapshot snapshot = carregarSnapshotPainel(authorization);
        List<GarcomResumoResponse> garcons = snapshot.garcons();
        List<MesaPainelCalculo> mesas = snapshot.mesas();

        int mesasLivres = (int) mesas.stream()
                .filter(mesa -> STATUS_MESA_DISPONIVEL.equals(mesa.mesa().status()))
                .count();
        int emPreparo = (int) mesas.stream()
                .filter(mesa -> STATUS_PAINEL_EM_PREPARO.equals(mesa.statusPedido()))
                .count();
        int prontos = (int) mesas.stream()
                .filter(mesa -> STATUS_PAINEL_PRONTO_ENTREGA.equals(mesa.statusPedido()))
                .count();
        int problemas = (int) mesas.stream()
                .filter(mesa -> CRITICIDADE_CRITICO.equals(mesa.criticidade()))
                .count();

        List<MesaPainelCalculo> atendimentosAtivos = mesas.stream()
                .filter(this::isAtendimentoAtivo)
                .toList();
        BigDecimal arrecadadoAtivos = atendimentosAtivos.stream()
                .map(MesaPainelCalculo::valorConta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ticketMedio = atendimentosAtivos.isEmpty()
                ? null
                : arrecadadoAtivos.divide(BigDecimal.valueOf(atendimentosAtivos.size()), 2, RoundingMode.HALF_UP);

        Map<Integer, Long> mesasAtivasPorGarcom = mesas.stream()
                .filter(mesa -> STATUS_MESA_OCUPADA.equals(mesa.mesa().status()))
                .filter(mesa -> mesa.mesa().garcomId() != null)
                .collect(Collectors.groupingBy(mesa -> mesa.mesa().garcomId(), Collectors.counting()));

        List<CargaGarcomResponse> cargaGarcons = garcons.stream()
                .map(garcom -> new CargaGarcomResponse(
                        garcom.id(),
                        garcom.nome(),
                        mesasAtivasPorGarcom.getOrDefault(garcom.id(), 0L).intValue()
                ))
                .toList();

        return new ResumoPainelResponse(mesasLivres, emPreparo, prontos, problemas, ticketMedio, cargaGarcons);
    }

    public List<GarcomResumoResponse> listarGarcons(String authorization) {
        PainelSnapshot snapshot = snapshotValido(authorization, System.currentTimeMillis());
        if (snapshot != null) {
            return snapshot.garcons();
        }

        return buscarGarcons(authorization);
    }

    public List<HistoricoAtendimentoResponse> listarHistoricoAtendimentos(String authorization) {
        List<HistoricoAtendimentoClientResponse> historicos = buscarHistoricoAtendimentos();
        Map<Integer, String> nomesGarconsPorId = buscarNomesGarconsPorIdQuandoNecessario(authorization, historicos);

        return historicos.stream()
                .map(historico -> mapearHistoricoAtendimento(historico, nomesGarconsPorId))
                .toList();
    }

    public MesaGestorResponse abrirMesa(Integer id, String authorization) {
        MesaClientResponse mesa = executarAlteracaoMesa(() -> mesaClient.abrirMesa(id));
        return mapearMesa(
                mesa,
                buscarGarconsPorIdQuandoNecessario(authorization, List.of(mesa)),
                buscarPedidosPorAtendimento(List.of(mesa))
        );
    }

    public MesaGestorResponse fecharMesa(Integer id, String authorization) {
        MesaClientResponse mesa = executarAlteracaoMesa(() -> mesaClient.fecharMesa(id));
        return mapearMesa(
                mesa,
                buscarGarconsPorIdQuandoNecessario(authorization, List.of(mesa)),
                buscarPedidosPorAtendimento(List.of(mesa))
        );
    }

    public MesaGestorResponse atribuirGarcom(Integer id, AtribuirGarcomRequest request, String authorization) {
        GarcomResumoResponse garcom = buscarGarcomPorId(request.garcomId(), authorization);
        MesaClientResponse mesa = executarAlteracaoMesa(() -> mesaClient.atribuirGarcom(
                id,
                new AtribuirGarcomClientRequest(request.garcomId())
        ));

        List<PedidoGestorResponse> pedidos = buscarPedidosPorAtendimento(List.of(mesa))
                .getOrDefault(mesa.idAtendimento(), List.of());

        return mesaGestorResponseMapper.map(new MesaGestorMapperSource(mesa, garcom.nome(), pedidos));
    }

    public MesaGestorResponse marcarEntregue(Integer id, String authorization) {
        MesaClientResponse mesa = buscarMesas().stream()
                .filter(item -> id.equals(item.id()))
                .findFirst()
                .orElseThrow(() -> new BaseException(ErrorEnum.MESA_NAO_ENCONTRADA));

        if (mesa.idAtendimento() == null) {
            throw new BaseException(ErrorEnum.ATENDIMENTO_NAO_ABERTO);
        }

        List<PedidoCozinhaResponse> pedidosAtivos = buscarPedidosAtivosDetalhados(List.of(mesa.idAtendimento()))
                .getOrDefault(mesa.idAtendimento(), List.of());

        pedidosAtivos.stream()
                .filter(pedido -> STATUS_PEDIDO_PRONTO.equals(pedido.status()))
                .forEach(pedido -> entregarPedido(pedido.id()));

        invalidarCachePainel();

        return mapearMesa(
                mesa,
                buscarGarconsPorIdQuandoNecessario(authorization, List.of(mesa)),
                buscarPedidosPorAtendimento(List.of(mesa))
        );
    }

    private void entregarPedido(Integer idPedido) {
        try {
            pedidoClient.entregarPedido(idPedido);
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private List<MesaClientResponse> buscarMesas() {
        try {
            return mesaClient.listarMesas();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private List<MesaPainelCalculo> carregarMesasPainel(String authorization) {
        List<MesaClientResponse> mesas = buscarMesas();
        Map<Integer, GarcomResumoResponse> garconsPorId = buscarGarconsPorIdQuandoNecessario(authorization, mesas);

        return carregarMesasPainel(mesas, garconsPorId);
    }

    private PainelSnapshot carregarSnapshotPainel(String authorization) {
        validarAuthorization(authorization);

        long agora = System.currentTimeMillis();
        PainelSnapshot snapshot = snapshotValido(authorization, agora);
        if (snapshot != null) {
            return snapshot;
        }

        synchronized (painelCacheLock) {
            snapshot = snapshotValido(authorization, System.currentTimeMillis());
            if (snapshot != null) {
                return snapshot;
            }

            PainelSnapshot novoSnapshot = carregarSnapshotPainelSemCache(authorization);
            painelSnapshot = novoSnapshot;
            return novoSnapshot;
        }
    }

    private PainelSnapshot snapshotValido(String authorization, long agora) {
        PainelSnapshot snapshot = painelSnapshot;
        if (snapshot == null
                || snapshot.expiraEmMillis() <= agora
                || !Objects.equals(snapshot.authorization(), authorization)) {
            return null;
        }

        return snapshot;
    }

    private PainelSnapshot carregarSnapshotPainelSemCache(String authorization) {
        List<MesaClientResponse> mesas = buscarMesas();

        CompletableFuture<List<GarcomResumoResponse>> garconsFuture =
                CompletableFuture.supplyAsync(() -> buscarGarcons(authorization));
        CompletableFuture<Map<Integer, List<PedidoGestorResponse>>> pedidosFuture =
                CompletableFuture.supplyAsync(() -> buscarPedidosPorAtendimento(mesas));

        List<GarcomResumoResponse> garcons = aguardar(garconsFuture);
        Map<Integer, List<PedidoGestorResponse>> pedidosPorAtendimento = aguardar(pedidosFuture);
        Map<Integer, GarcomResumoResponse> garconsPorId = garcons.stream()
                .collect(Collectors.toMap(GarcomResumoResponse::id, Function.identity()));

        List<MesaPainelCalculo> mesasPainel = mesas.stream()
                .map(mesa -> criarMesaPainelCalculo(mesa, garconsPorId, pedidosPorAtendimento))
                .toList();

        return new PainelSnapshot(
                authorization,
                System.currentTimeMillis() + CACHE_PAINEL_TTL_MILLIS,
                mesasPainel,
                garcons
        );
    }

    private <T> T aguardar(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof BaseException baseException) {
                throw baseException;
            }

            throw e;
        }
    }

    private void invalidarCachePainel() {
        painelSnapshot = null;
    }

    private List<MesaPainelCalculo> carregarMesasPainel(
            List<MesaClientResponse> mesas,
            Map<Integer, GarcomResumoResponse> garconsPorId
    ) {
        Map<Integer, List<PedidoGestorResponse>> pedidosPorAtendimento = buscarPedidosPorAtendimento(mesas);

        return mesas.stream()
                .map(mesa -> criarMesaPainelCalculo(mesa, garconsPorId, pedidosPorAtendimento))
                .toList();
    }

    private MesaPainelCalculo criarMesaPainelCalculo(
            MesaClientResponse mesa,
            Map<Integer, GarcomResumoResponse> garconsPorId,
            Map<Integer, List<PedidoGestorResponse>> pedidosPorAtendimento
    ) {
        GarcomResumoResponse garcom = mesa.garcomId() == null ? null : garconsPorId.get(mesa.garcomId());
        List<PedidoGestorResponse> pedidos = mesa.idAtendimento() == null
                ? List.of()
                : pedidosPorAtendimento.getOrDefault(mesa.idAtendimento(), List.of());
        String statusPedido = derivarStatusPedido(pedidos);
        Long tempoMinutos = calcularTempoMinutos(pedidos);
        BigDecimal valorConta = calcularValorConta(pedidos);

        return new MesaPainelCalculo(
                mesa,
                garcom == null ? null : garcom.nome(),
                pedidos,
                statusPedido,
                tempoMinutos,
                calcularCriticidade(mesa, statusPedido, tempoMinutos),
                valorConta
        );
    }

    private List<HistoricoAtendimentoClientResponse> buscarHistoricoAtendimentos() {
        try {
            List<HistoricoAtendimentoClientResponse> historicos = mesaClient.listarHistoricoAtendimentos();
            return historicos == null ? List.of() : historicos;
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private Map<Integer, List<PedidoGestorResponse>> buscarPedidosPorAtendimento(List<MesaClientResponse> mesas) {
        return buscarPedidosAtivosDetalhados(idsAtendimentoAbertos(mesas))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream().map(this::mapearPedidoGestor).toList()
                ));
    }

    private Map<Integer, List<PedidoCozinhaResponse>> buscarPedidosAtivosDetalhados(List<Integer> idsAtendimento) {
        if (idsAtendimento.isEmpty()) {
            return Map.of();
        }

        try {
            return pedidoClient.listarPedidosAtivosDetalhadosPorAtendimentos(idsAtendimento)
                    .stream()
                    .collect(Collectors.groupingBy(PedidoCozinhaResponse::idAtendimento));
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private List<Integer> idsAtendimentoAbertos(List<MesaClientResponse> mesas) {
        return mesas.stream()
                .map(MesaClientResponse::idAtendimento)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private PedidoGestorResponse mapearPedidoGestor(PedidoCozinhaResponse pedido) {
        BigDecimal valor = pedido.itens().stream()
                .map(this::valorItem)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItens = pedido.itens().stream()
                .mapToInt(ItemPedidoCozinhaResponse::quantidade)
                .sum();

        return new PedidoGestorResponse(pedido.id(), pedido.status(), valor, pedido.dataCriacao(), totalItens);
    }

    private BigDecimal valorItem(ItemPedidoCozinhaResponse item) {
        if (item.precoUnitario() == null) {
            return BigDecimal.ZERO;
        }

        return item.precoUnitario().multiply(BigDecimal.valueOf(item.quantidade()));
    }

    private List<GarcomResumoResponse> buscarGarcons(String authorization) {
        validarAuthorization(authorization);

        try {
            return usuarioClient.listarUsuariosAtivos(authorization)
                    .stream()
                    .filter(this::isGarcomAtivo)
                    .sorted(Comparator.comparing(UsuarioClientResponse::nome))
                    .map(garcomResumoResponseMapper::map)
                    .toList();
        } catch (FeignException e) {
            if (e.status() == 401) {
                throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
            }

            if (e.status() == 403) {
                throw new BaseException(ErrorEnum.ACESSO_NEGADO);
            }

            throw new BaseException(ErrorEnum.MS_USUARIOS_INDISPONIVEL);
        }
    }

    private Map<Integer, GarcomResumoResponse> buscarGarconsPorIdQuandoNecessario(
            String authorization,
            List<MesaClientResponse> mesas
    ) {
        boolean possuiGarcomAtribuido = mesas.stream()
                .anyMatch(mesa -> mesa.garcomId() != null);

        if (!possuiGarcomAtribuido) {
            return Map.of();
        }

        return buscarGarcons(authorization)
                .stream()
                .collect(Collectors.toMap(GarcomResumoResponse::id, Function.identity()));
    }

    private Map<Integer, String> buscarNomesGarconsPorIdQuandoNecessario(
            String authorization,
            List<HistoricoAtendimentoClientResponse> historicos
    ) {
        boolean precisaBuscarGarcom = historicos.stream()
                .anyMatch(historico -> historico.idGarcom() != null && nomeEmBranco(historico.nomeGarcom()));

        if (!precisaBuscarGarcom) {
            return Map.of();
        }

        return buscarGarcons(authorization)
                .stream()
                .collect(Collectors.toMap(GarcomResumoResponse::id, GarcomResumoResponse::nome));
    }

    private GarcomResumoResponse buscarGarcomPorId(Integer garcomId, String authorization) {
        Optional<GarcomResumoResponse> garcom = buscarGarcons(authorization)
                .stream()
                .filter(response -> response.id().equals(garcomId))
                .findFirst();

        return garcom.orElseThrow(() -> new BaseException(ErrorEnum.GARCOM_INVALIDO));
    }

    private MesaGestorResponse mapearMesa(
            MesaClientResponse mesa,
            Map<Integer, GarcomResumoResponse> garconsPorId,
            Map<Integer, List<PedidoGestorResponse>> pedidosPorAtendimento
    ) {
        GarcomResumoResponse garcom = mesa.garcomId() == null ? null : garconsPorId.get(mesa.garcomId());
        List<PedidoGestorResponse> pedidos = mesa.idAtendimento() == null
                ? List.of()
                : pedidosPorAtendimento.getOrDefault(mesa.idAtendimento(), List.of());

        return mesaGestorResponseMapper.map(new MesaGestorMapperSource(
                mesa,
                garcom == null ? null : garcom.nome(),
                pedidos
        ));
    }

    private MesaGestorResponse mapearMesa(MesaPainelCalculo mesa) {
        return mesaGestorResponseMapper.map(new MesaGestorMapperSource(
                mesa.mesa(),
                mesa.garcomNome(),
                mesa.pedidos()
        ));
    }

    private boolean aplicarFiltroEstado(MesaPainelCalculo mesa, String filtroEstado) {
        if (filtroEstado == null || filtroEstado.isBlank()) {
            return true;
        }

        return switch (filtroEstado.trim().toUpperCase(Locale.ROOT)) {
            case "PROBLEMAS", "ATRASADAS" -> CRITICIDADE_CRITICO.equals(mesa.criticidade());
            case "PRONTOS" -> STATUS_PAINEL_PRONTO_ENTREGA.equals(mesa.statusPedido());
            case "EM_PREPARO" -> STATUS_PAINEL_EM_PREPARO.equals(mesa.statusPedido());
            case "LIVRE" -> STATUS_MESA_DISPONIVEL.equals(mesa.mesa().status());
            case "SEM_GARCOM" -> STATUS_MESA_OCUPADA.equals(mesa.mesa().status()) && mesa.mesa().garcomId() == null;
            case "CONTA_ABERTA" -> STATUS_PAINEL_CONTA_ABERTA.equals(mesa.statusPedido());
            case "AGUARDANDO_PEDIDO" -> STATUS_MESA_OCUPADA.equals(mesa.mesa().status())
                    && mesa.mesa().garcomId() != null
                    && mesa.pedidos().isEmpty();
            default -> true;
        };
    }

    private boolean aplicarBusca(MesaPainelCalculo mesa, String busca) {
        if (busca == null || busca.isBlank()) {
            return true;
        }

        String termo = busca.trim().toLowerCase(Locale.ROOT);
        String numero = String.valueOf(mesa.mesa().numero());
        String garcomNome = mesa.garcomNome() == null ? "" : mesa.garcomNome().toLowerCase(Locale.ROOT);
        String statusLabel = statusPedidoLabel(mesa.statusPedido());

        return numero.contains(termo) || garcomNome.contains(termo) || statusLabel.contains(termo);
    }

    private Comparator<MesaPainelCalculo> comparadorMesas(String sort) {
        String ordenacao = sort == null || sort.isBlank()
                ? "numero,asc"
                : sort.trim().toLowerCase(Locale.ROOT);

        return switch (ordenacao) {
            case "numero,desc" -> Comparator.comparing((MesaPainelCalculo mesa) -> mesa.mesa().numero()).reversed();
            case "criticidade" -> Comparator.comparingInt(this::rankCriticidade);
            case "valor,desc" -> Comparator.comparing(
                    MesaPainelCalculo::valorConta,
                    Comparator.nullsLast(Comparator.reverseOrder())
            );
            case "valor,asc" -> Comparator.comparing(
                    MesaPainelCalculo::valorConta,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            default -> Comparator.comparing(mesa -> mesa.mesa().numero());
        };
    }

    private int rankCriticidade(MesaPainelCalculo mesa) {
        if (STATUS_MESA_OCUPADA.equals(mesa.mesa().status())) {
            if (STATUS_PAINEL_EM_PREPARO.equals(mesa.statusPedido())) {
                return 1;
            }

            if (STATUS_PAINEL_PRONTO_ENTREGA.equals(mesa.statusPedido())) {
                return 2;
            }

            return 3;
        }

        return 4;
    }

    private String derivarStatusPedido(List<PedidoGestorResponse> pedidos) {
        if (pedidos.isEmpty()) {
            return null;
        }

        if (pedidos.stream().anyMatch(pedido -> STATUS_PEDIDO_EM_PREPARO.contains(pedido.status()))) {
            return STATUS_PAINEL_EM_PREPARO;
        }

        if (pedidos.stream().anyMatch(pedido -> STATUS_PEDIDO_PRONTO.equals(pedido.status()))) {
            return STATUS_PAINEL_PRONTO_ENTREGA;
        }

        return STATUS_PAINEL_CONTA_ABERTA;
    }

    private Long calcularTempoMinutos(List<PedidoGestorResponse> pedidos) {
        return pedidos.stream()
                .map(PedidoGestorResponse::criadoEm)
                .filter(java.util.Objects::nonNull)
                .mapToLong(this::minutosDesde)
                .min()
                .stream()
                .boxed()
                .findFirst()
                .orElse(null);
    }

    private long minutosDesde(LocalDateTime dataCriacao) {
        return Math.max(0, ChronoUnit.MINUTES.between(dataCriacao, LocalDateTime.now()));
    }

    private String calcularCriticidade(MesaClientResponse mesa, String statusPedido, Long tempoMinutos) {
        if (STATUS_MESA_DISPONIVEL.equals(mesa.status())) {
            return "livre";
        }

        long tempo = tempoMinutos == null ? 0 : tempoMinutos;

        if (STATUS_PAINEL_EM_PREPARO.equals(statusPedido)) {
            return tempo >= LIMIAR_PREPARO_ATENCAO_MINUTOS ? "atencao" : "emAndamento";
        }

        if (STATUS_PAINEL_PRONTO_ENTREGA.equals(statusPedido)) {
            if (tempo > LIMIAR_PRONTO_ATENCAO_MINUTOS) {
                return CRITICIDADE_CRITICO;
            }

            if (tempo > LIMIAR_PRONTO_OK_MINUTOS) {
                return "atencao";
            }

            return "ok";
        }

        if (STATUS_PAINEL_CONTA_ABERTA.equals(statusPedido)) {
            return "info";
        }

        return "emAndamento";
    }

    private BigDecimal calcularValorConta(List<PedidoGestorResponse> pedidos) {
        if (pedidos.isEmpty()) {
            return null;
        }

        return pedidos.stream()
                .map(PedidoGestorResponse::valor)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String statusPedidoLabel(String statusPedido) {
        if (STATUS_PAINEL_EM_PREPARO.equals(statusPedido)) {
            return "em preparo";
        }

        if (STATUS_PAINEL_PRONTO_ENTREGA.equals(statusPedido)) {
            return "pronto para entrega";
        }

        if (STATUS_PAINEL_CONTA_ABERTA.equals(statusPedido)) {
            return "conta aberta";
        }

        return "";
    }

    private boolean isAtendimentoAtivo(MesaPainelCalculo mesa) {
        return STATUS_MESA_OCUPADA.equals(mesa.mesa().status())
                && mesa.mesa().garcomId() != null
                && !mesa.pedidos().isEmpty();
    }

    private int normalizarPage(Integer page) {
        return page == null ? 0 : Math.max(0, page);
    }

    private int normalizarSize(Integer size) {
        return size == null ? 10 : Math.max(1, size);
    }

    private int calcularTotalPaginas(long totalElements, int size) {
        if (totalElements == 0) {
            return 0;
        }

        return (int) Math.ceil((double) totalElements / size);
    }

    private HistoricoAtendimentoResponse mapearHistoricoAtendimento(
            HistoricoAtendimentoClientResponse historico,
            Map<Integer, String> nomesGarconsPorId
    ) {
        String nomeGarcom = nomeEmBranco(historico.nomeGarcom())
                ? nomesGarconsPorId.get(historico.idGarcom())
                : historico.nomeGarcom();

        return new HistoricoAtendimentoResponse(
                historico.id(),
                historico.idAtendimento(),
                historico.codigoSessao(),
                historico.idMesa(),
                historico.numeroMesa(),
                historico.idGarcom(),
                nomeGarcom,
                historico.valorFinal(),
                historico.totalPedidos(),
                historico.totalItens(),
                historico.dataAbertura(),
                historico.dataFechamento(),
                historico.duracaoMinutos()
        );
    }

    private boolean nomeEmBranco(String nome) {
        return nome == null || nome.isBlank();
    }

    private MesaClientResponse executarAlteracaoMesa(AlteracaoMesa alteracaoMesa) {
        try {
            MesaClientResponse mesa = alteracaoMesa.executar();
            invalidarCachePainel();
            return mesa;
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new BaseException(ErrorEnum.MESA_NAO_ENCONTRADA);
            }

            if (e.status() == 400 || e.status() == 409) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private boolean isGarcomAtivo(UsuarioClientResponse usuario) {
        return Boolean.TRUE.equals(usuario.ativo())
                && PERFIL_GARCOM.equals(usuario.perfilUsuario());
    }

    private void validarAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }
    }

    @FunctionalInterface
    private interface AlteracaoMesa {
        MesaClientResponse executar();
    }

    private record MesaPainelCalculo(
            MesaClientResponse mesa,
            String garcomNome,
            List<PedidoGestorResponse> pedidos,
            String statusPedido,
            Long tempoMinutos,
            String criticidade,
            BigDecimal valorConta
    ) {
    }

    private record PainelSnapshot(
            String authorization,
            long expiraEmMillis,
            List<MesaPainelCalculo> mesas,
            List<GarcomResumoResponse> garcons
    ) {
    }
}
