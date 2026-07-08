package br.com.fourkitchen.ms_pedidos.service;

import br.com.fourkitchen.ms_pedidos.dto.request.CriarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.DecisaoProblemaRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.ProdutoPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.SinalizarProblemaRequest;
import br.com.fourkitchen.ms_pedidos.dto.response.PedidoCozinhaResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.PedidoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.ResumoContaAtendimentoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.ResumoPedidosOperacaoResponse;
import br.com.fourkitchen.ms_pedidos.dto.response.SinalizarProblemaResponse;
import br.com.fourkitchen.ms_pedidos.entities.Pedido;
import br.com.fourkitchen.ms_pedidos.entities.ProdutoPedido;
import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusProdutoPedido;
import br.com.fourkitchen.ms_pedidos.exceptions.*;
import br.com.fourkitchen.ms_pedidos.mapper.CriarPedidoRequestMapper;
import br.com.fourkitchen.ms_pedidos.mapper.PedidoResponseMapper;
import br.com.fourkitchen.ms_pedidos.repository.PedidoRepository;
import br.com.fourkitchen.ms_pedidos.repository.ProdutoPedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoPedidoRepository produtoPedidoRepository;

    @Mock
    private PedidoResponseMapper pedidoResponseMapper;

    @Mock
    private CriarPedidoRequestMapper criarPedidoRequestMapper;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void createPedidoDeveGerarCodigoEnviarParaCozinhaVincularAtendimentoESalvarItens() {
        ProdutoPedidoRequest item = new ProdutoPedidoRequest(10, "X-Burger", 2, new BigDecimal("29.90"), "Sem cebola");
        CriarPedidoRequest request = new CriarPedidoRequest(
                null,
                null,
                CanaisPedido.MESA,
                StatusPedido.ENVIADO_COZINHA,
                1,
                null,
                8,
                List.of(item)
        );
        Pedido pedido = Pedido.builder()
                .canal(CanaisPedido.MESA)
                .idMesa(1)
                .idAtendimento(8)
                .build();
        PedidoResponse response = new PedidoResponse(
                25,
                123456,
                CanaisPedido.MESA,
                StatusPedido.ENVIADO_COZINHA,
                1,
                null,
                8
        );

        when(criarPedidoRequestMapper.map(request)).thenReturn(pedido);
        when(pedidoRepository.existsByCodigo(anyInt())).thenReturn(false);
        when(pedidoRepository.save(pedido)).thenAnswer(invocation -> {
            pedido.setId(25);
            return pedido;
        });
        when(pedidoResponseMapper.map(pedido)).thenReturn(response);

        PedidoResponse resultado = pedidoService.createPedido(request);

        assertSame(response, resultado);
        assertNotNull(pedido.getCodigo());
        assertEquals(StatusPedido.ENVIADO_COZINHA, pedido.getStatus());
        assertEquals(8, pedido.getIdAtendimento());

        ArgumentCaptor<ProdutoPedido> produtoPedidoCaptor = ArgumentCaptor.forClass(ProdutoPedido.class);
        verify(produtoPedidoRepository).save(produtoPedidoCaptor.capture());

        ProdutoPedido produtoPedido = produtoPedidoCaptor.getValue();
        assertEquals(25, produtoPedido.getIdPedido());
        assertEquals(10, produtoPedido.getIdProduto());
        assertEquals("X-Burger", produtoPedido.getNomeProduto());
        assertEquals(2, produtoPedido.getQuantidade());
        assertEquals(new BigDecimal("29.90"), produtoPedido.getPrecoUnitario());
        assertEquals("Sem cebola", produtoPedido.getObservacao());
        verify(pedidoRepository).save(pedido);
        verify(pedidoResponseMapper).map(pedido);
    }

    @Test
    void createPedidoTotemDevePermitirPedidoSemMesa() {
        ProdutoPedidoRequest item = new ProdutoPedidoRequest(10, "X-Burger", 2, new BigDecimal("29.90"), "Sem cebola");
        CriarPedidoRequest request = new CriarPedidoRequest(
                null,
                null,
                CanaisPedido.TOTEM,
                StatusPedido.ENVIADO_COZINHA,
                null,
                null,
                null,
                List.of(item)
        );
        Pedido pedido = Pedido.builder()
                .canal(CanaisPedido.TOTEM)
                .build();
        PedidoResponse response = new PedidoResponse(
                25,
                123456,
                CanaisPedido.TOTEM,
                StatusPedido.ENVIADO_COZINHA,
                null,
                null,
                null
        );

        when(criarPedidoRequestMapper.map(request)).thenReturn(pedido);
        when(pedidoRepository.existsByCodigo(anyInt())).thenReturn(false);
        when(pedidoRepository.save(pedido)).thenAnswer(invocation -> {
            pedido.setId(25);
            return pedido;
        });
        when(pedidoResponseMapper.map(pedido)).thenReturn(response);

        PedidoResponse resultado = pedidoService.createPedido(request);

        assertSame(response, resultado);
        assertNotNull(pedido.getCodigo());
        assertEquals(CanaisPedido.TOTEM, pedido.getCanal());
        assertEquals(StatusPedido.ENVIADO_COZINHA, pedido.getStatus());
        assertEquals(null, pedido.getIdMesa());

        ArgumentCaptor<ProdutoPedido> produtoPedidoCaptor = ArgumentCaptor.forClass(ProdutoPedido.class);
        verify(produtoPedidoRepository).save(produtoPedidoCaptor.capture());

        ProdutoPedido produtoPedido = produtoPedidoCaptor.getValue();
        assertEquals(25, produtoPedido.getIdPedido());
        assertEquals(10, produtoPedido.getIdProduto());
        assertEquals("X-Burger", produtoPedido.getNomeProduto());
        assertEquals(2, produtoPedido.getQuantidade());
        assertEquals(new BigDecimal("29.90"), produtoPedido.getPrecoUnitario());
        assertEquals("Sem cebola", produtoPedido.getObservacao());
        verify(pedidoRepository).save(pedido);
        verify(pedidoResponseMapper).map(pedido);
    }

    @Test
    void possuiPedidosAtivosDeveConsultarPedidosAtivosDoAtendimento() {
        when(pedidoRepository.existsByIdAtendimentoAndStatusIn(eq(8), anyStatusCollection())).thenReturn(true);

        boolean resultado = pedidoService.possuiPedidosAtivos(8);

        assertEquals(true, resultado);
        verify(pedidoRepository).existsByIdAtendimentoAndStatusIn(eq(8), anyStatusCollection());
    }

    @Test
    void findPedidosAtivosPorAtendimentosDeveConsultarPedidosAtivosDosAtendimentos() {
        Pedido pedido = Pedido.builder()
                .id(25)
                .codigo(123456)
                .canal(CanaisPedido.GARCOM)
                .status(StatusPedido.ENVIADO_COZINHA)
                .idMesa(1)
                .idUsuario(7)
                .idAtendimento(8)
                .build();
        PedidoResponse response = new PedidoResponse(
                25,
                123456,
                CanaisPedido.GARCOM,
                StatusPedido.ENVIADO_COZINHA,
                1,
                7,
                8
        );

        when(pedidoRepository.findByIdAtendimentoInAndStatusInOrderByDataCriacaoAscIdAsc(
                eq(List.of(8)),
                anyStatusCollection()
        )).thenReturn(List.of(pedido));
        when(pedidoResponseMapper.map(pedido)).thenReturn(response);

        List<PedidoResponse> resultado = pedidoService.findPedidosAtivosPorAtendimentos(List.of(8));

        assertEquals(List.of(response), resultado);
        verify(pedidoRepository).findByIdAtendimentoInAndStatusInOrderByDataCriacaoAscIdAsc(
                eq(List.of(8)),
                anyStatusCollection()
        );
        verify(pedidoResponseMapper).map(pedido);
    }

    @Test
    void findPedidosAtivosPorAtendimentosDeveRetornarListaVaziaQuandoNaoReceberAtendimentos() {
        List<PedidoResponse> resultado = pedidoService.findPedidosAtivosPorAtendimentos(List.of());

        assertEquals(List.of(), resultado);
        verifyNoInteractions(pedidoRepository, pedidoResponseMapper);
    }

    @Test
    void findPedidosCozinhaDeveRetornarPedidosEmStatusDeCozinha() {
        Pedido pedido = Pedido.builder()
                .id(25)
                .codigo(123456)
                .canal(CanaisPedido.MESA)
                .status(StatusPedido.ENVIADO_COZINHA)
                .idMesa(1)
                .idAtendimento(8)
                .build();
        PedidoResponse response = new PedidoResponse(
                25,
                123456,
                CanaisPedido.MESA,
                StatusPedido.ENVIADO_COZINHA,
                1,
                null,
                8
        );

        when(pedidoRepository.findByStatusIn(anyStatusCollection())).thenReturn(List.of(pedido));
        when(pedidoResponseMapper.map(pedido)).thenReturn(response);

        List<PedidoResponse> resultado = pedidoService.findPedidosCozinha();

        assertEquals(List.of(response), resultado);
        verify(pedidoRepository).findByStatusIn(anyStatusCollection());
        verify(pedidoResponseMapper).map(pedido);
    }

    @Test
    void findFilaCozinhaDeveRetornarPedidosComItensOrdenadosPorChegada() {
        LocalDateTime dataCriacao = LocalDateTime.of(2026, 7, 2, 10, 30);

        Pedido pedido = Pedido.builder()
                .id(25)
                .codigo(123456)
                .canal(CanaisPedido.MESA)
                .status(StatusPedido.ENVIADO_COZINHA)
                .idMesa(1)
                .idAtendimento(8)
                .dataCriacao(dataCriacao)
                .build();

        ProdutoPedido item = ProdutoPedido.builder()
                .id(5)
                .idPedido(25)
                .idProduto(10)
                .nomeProduto("X-Burger")
                .quantidade(2)
                .precoUnitario(new BigDecimal("29.90"))
                .observacao("Sem cebola")
                .build();

        when(pedidoRepository.findByStatusInOrderByDataCriacaoAscIdAsc(anyStatusCollection()))
                .thenReturn(List.of(pedido));

        when(produtoPedidoRepository.findByIdPedidoIn(List.of(25)))
                .thenReturn(List.of(item));

        List<PedidoCozinhaResponse> resultado = pedidoService.findFilaCozinha();

        assertEquals(1, resultado.size());

        PedidoCozinhaResponse pedidoResponse = resultado.getFirst();

        assertEquals(25, pedidoResponse.id());
        assertEquals(123456, pedidoResponse.codigo());
        assertEquals(CanaisPedido.MESA, pedidoResponse.canal());
        assertEquals(StatusPedido.ENVIADO_COZINHA, pedidoResponse.status());
        assertEquals(1, pedidoResponse.idMesa());
        assertEquals(8, pedidoResponse.idAtendimento());
        assertEquals(dataCriacao, pedidoResponse.dataCriacao());

        assertEquals(1, pedidoResponse.itens().size());
        assertEquals(5, pedidoResponse.itens().getFirst().id());
        assertEquals(10, pedidoResponse.itens().getFirst().idProduto());
        assertEquals("X-Burger", pedidoResponse.itens().getFirst().nomeProduto());
        assertEquals(2, pedidoResponse.itens().getFirst().quantidade());
        assertEquals(new BigDecimal("29.90"), pedidoResponse.itens().getFirst().precoUnitario());
        assertEquals("Sem cebola", pedidoResponse.itens().getFirst().observacao());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<StatusPedido>> statusCaptor =
                ArgumentCaptor.forClass(Collection.class);

        verify(pedidoRepository)
                .findByStatusInOrderByDataCriacaoAscIdAsc(statusCaptor.capture());

        assertIterableEquals(
                List.of(
                        StatusPedido.ENVIADO_COZINHA,
                        StatusPedido.EM_PREPARO
                ),
                statusCaptor.getValue()
        );

        verify(produtoPedidoRepository).findByIdPedidoIn(List.of(25));
    }

    @Test
    void findFilaCozinhaDeveRetornarListaVaziaSemBuscarItensQuandoNaoHaPedidos() {
        when(pedidoRepository.findByStatusInOrderByDataCriacaoAscIdAsc(anyStatusCollection())).thenReturn(List.of());

        List<PedidoCozinhaResponse> resultado = pedidoService.findFilaCozinha();

        assertEquals(List.of(), resultado);
        verify(pedidoRepository).findByStatusInOrderByDataCriacaoAscIdAsc(anyStatusCollection());
    }

    @Test
    void iniciarPreparoDeveAlterarPedidoEnviadoParaEmPreparo() {
        Pedido pedido = Pedido.builder()
                .id(25)
                .codigo(123456)
                .canal(CanaisPedido.GARCOM)
                .status(StatusPedido.ENVIADO_COZINHA)
                .idMesa(1)
                .idUsuario(7)
                .idAtendimento(8)
                .build();
        PedidoResponse response = new PedidoResponse(
                25,
                123456,
                CanaisPedido.GARCOM,
                StatusPedido.EM_PREPARO,
                1,
                7,
                8
        );

        when(pedidoRepository.findById(25)).thenReturn(Optional.of(pedido));
        when(pedidoResponseMapper.map(pedido)).thenReturn(response);

        PedidoResponse resultado = pedidoService.iniciarPreparo(25);

        assertSame(response, resultado);
        assertEquals(StatusPedido.EM_PREPARO, pedido.getStatus());
        verify(pedidoRepository).findById(25);
        verify(pedidoResponseMapper).map(pedido);
    }

    @Test
    void finalizarPreparoDeveAlterarPedidoEmPreparoParaPronto() {
        Pedido pedido = Pedido.builder()
                .id(25)
                .codigo(123456)
                .canal(CanaisPedido.GARCOM)
                .status(StatusPedido.EM_PREPARO)
                .idMesa(1)
                .idUsuario(7)
                .idAtendimento(8)
                .build();
        PedidoResponse response = new PedidoResponse(
                25,
                123456,
                CanaisPedido.GARCOM,
                StatusPedido.PRONTO,
                1,
                7,
                8
        );

        when(pedidoRepository.findById(25)).thenReturn(Optional.of(pedido));
        when(pedidoResponseMapper.map(pedido)).thenReturn(response);

        PedidoResponse resultado = pedidoService.finalizarPreparo(25);

        assertSame(response, resultado);
        assertEquals(StatusPedido.PRONTO, pedido.getStatus());
        verify(pedidoRepository).findById(25);
        verify(pedidoResponseMapper).map(pedido);
    }

    @Test
    void iniciarPreparoDeveBloquearTransicaoInvalida() {
        Pedido pedido = Pedido.builder()
                .id(25)
                .status(StatusPedido.EM_PREPARO)
                .build();

        when(pedidoRepository.findById(25)).thenReturn(Optional.of(pedido));

        BaseException exception = assertThrows(BaseException.class, () -> pedidoService.iniciarPreparo(25));

        assertEquals(ErrorEnum.TRANSICAO_STATUS_INVALIDA, exception.getErrorEnum());
        assertEquals(StatusPedido.EM_PREPARO, pedido.getStatus());
        verify(pedidoRepository).findById(25);
        verify(pedidoResponseMapper, never()).map(any(Pedido.class));
    }

    @Test
    void finalizarPreparoDeveBloquearTransicaoInvalida() {
        Pedido pedido = Pedido.builder()
                .id(25)
                .status(StatusPedido.ENVIADO_COZINHA)
                .build();

        when(pedidoRepository.findById(25)).thenReturn(Optional.of(pedido));

        BaseException exception = assertThrows(BaseException.class, () -> pedidoService.finalizarPreparo(25));

        assertEquals(ErrorEnum.TRANSICAO_STATUS_INVALIDA, exception.getErrorEnum());
        assertEquals(StatusPedido.ENVIADO_COZINHA, pedido.getStatus());
        verify(pedidoRepository).findById(25);
        verify(pedidoResponseMapper, never()).map(any(Pedido.class));
    }

    @Test
    void buscarResumoOperacaoDeveContarPedidosPorStatus() {
        when(pedidoRepository.countByStatus(StatusPedido.EM_PREPARO)).thenReturn(5L);
        when(pedidoRepository.countByStatus(StatusPedido.PRONTO)).thenReturn(3L);
        when(pedidoRepository.countByStatus(StatusPedido.AGUARDANDO_DECISAO)).thenReturn(2L);

        ResumoPedidosOperacaoResponse resultado = pedidoService.buscarResumoOperacao();

        assertEquals(5L, resultado.pedidosEmPreparo());
        assertEquals(3L, resultado.pedidosProntos());
        assertEquals(2L, resultado.problemasPendentes());
        verify(pedidoRepository).countByStatus(StatusPedido.EM_PREPARO);
        verify(pedidoRepository).countByStatus(StatusPedido.PRONTO);
        verify(pedidoRepository).countByStatus(StatusPedido.AGUARDANDO_DECISAO);
    }

    @Test
    void buscarResumoContaAtendimentoDeveSomarItensDePedidosNaoCancelados() {
        Pedido pedido1 = Pedido.builder()
                .id(25)
                .idAtendimento(8)
                .status(StatusPedido.FINALIZADO)
                .build();
        Pedido pedido2 = Pedido.builder()
                .id(26)
                .idAtendimento(8)
                .status(StatusPedido.ENTREGUE)
                .build();
        ProdutoPedido item1 = ProdutoPedido.builder()
                .idPedido(25)
                .quantidade(2)
                .precoUnitario(new BigDecimal("29.90"))
                .build();
        ProdutoPedido item2 = ProdutoPedido.builder()
                .idPedido(26)
                .quantidade(1)
                .precoUnitario(new BigDecimal("15.50"))
                .build();

        when(pedidoRepository.findByIdAtendimentoAndStatusNotOrderByDataCriacaoAscIdAsc(
                8,
                StatusPedido.CANCELADO
        )).thenReturn(List.of(pedido1, pedido2));
        when(produtoPedidoRepository.findByIdPedidoIn(List.of(25, 26))).thenReturn(List.of(item1, item2));

        ResumoContaAtendimentoResponse resultado = pedidoService.buscarResumoContaAtendimento(8);

        assertEquals(8, resultado.idAtendimento());
        assertEquals(new BigDecimal("75.30"), resultado.valorFinal());
        assertEquals(2, resultado.totalPedidos());
        assertEquals(3, resultado.totalItens());
        verify(pedidoRepository).findByIdAtendimentoAndStatusNotOrderByDataCriacaoAscIdAsc(
                8,
                StatusPedido.CANCELADO
        );
        verify(produtoPedidoRepository).findByIdPedidoIn(List.of(25, 26));
    }

    @Test
    void buscarResumoContaAtendimentoDeveRetornarZeroQuandoNaoHouverPedidos() {
        when(pedidoRepository.findByIdAtendimentoAndStatusNotOrderByDataCriacaoAscIdAsc(
                8,
                StatusPedido.CANCELADO
        )).thenReturn(List.of());

        ResumoContaAtendimentoResponse resultado = pedidoService.buscarResumoContaAtendimento(8);

        assertEquals(8, resultado.idAtendimento());
        assertEquals(BigDecimal.ZERO, resultado.valorFinal());
        assertEquals(0, resultado.totalPedidos());
        assertEquals(0, resultado.totalItens());
        verify(pedidoRepository).findByIdAtendimentoAndStatusNotOrderByDataCriacaoAscIdAsc(
                8,
                StatusPedido.CANCELADO
        );
        verifyNoInteractions(produtoPedidoRepository);
    }

    @ParameterizedTest
    @EnumSource(value = StatusProdutoPedido.class, names = {"FALTA_PRODUTO", "ERRO", "INDISPONIVEL"})
    void sinalizarProblema_deveAlterarStatusDoPedidoEProduto_paraDiferentesTiposDeProblema(StatusProdutoPedido statusProblema) {
        // Arrange
        SinalizarProblemaRequest request = new SinalizarProblemaRequest(1, 10, statusProblema);

        Pedido pedido = Pedido.builder()
                .id(1)
                .status(StatusPedido.EM_PREPARO)
                .build();

        ProdutoPedido produtoPedido = ProdutoPedido.builder()
                .id(10)
                .idPedido(1)
                .status(null)
                .build();

        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));
        when(produtoPedidoRepository.findByIdPedidoAndId(1, 10)).thenReturn(Optional.of(produtoPedido));

        // Act
        SinalizarProblemaResponse response = pedidoService.sinalizarProblema(request);

        // Assert
        assertEquals(StatusPedido.AGUARDANDO_DECISAO, pedido.getStatus());
        assertEquals(statusProblema, produtoPedido.getStatus());

        assertNotNull(response);
        assertEquals(1, response.idPedido());
        assertEquals(10, response.idProdutoPedido());
        assertEquals(StatusPedido.AGUARDANDO_DECISAO, response.statusPedido());
        assertEquals(statusProblema, response.statusProdutoPedido());

        verify(pedidoRepository).findById(1);
        verify(produtoPedidoRepository).findByIdPedidoAndId(1, 10);
    }

    @Test
    void iniciarPreparo_deveLancarExcecao_quandoPedidoAguardandoDecisao() {
        // Arrange
        Pedido pedido = Pedido.builder()
                .id(25)
                .status(StatusPedido.AGUARDANDO_DECISAO)
                .build();

        when(pedidoRepository.findById(25)).thenReturn(Optional.of(pedido));

        // Act & Assert
        assertThrows(BaseException.class, () -> {
            pedidoService.iniciarPreparo(25);
        });

        // Verify
        verify(pedidoRepository).findById(25);
        verifyNoInteractions(pedidoResponseMapper);
    }

    @Test
    void finalizarPreparo_deveLancarExcecao_quandoPedidoAguardandoDecisao() {
        // Arrange
        Pedido pedido = Pedido.builder()
                .id(25)
                .status(StatusPedido.AGUARDANDO_DECISAO)
                .build();

        when(pedidoRepository.findById(25)).thenReturn(Optional.of(pedido));

        // Act & Assert
        assertThrows(BaseException.class, () -> {
            pedidoService.finalizarPreparo(25);
        });

        // Verify
        verify(pedidoRepository).findById(25);
        verifyNoInteractions(pedidoResponseMapper);
    }

    private Collection<StatusPedido> anyStatusCollection() {
        return any();
    }

    @Test
    void decisaoProblema_deveLancarExcecao_quandoPedidoNaoExistir() {
        DecisaoProblemaRequest request =
                new DecisaoProblemaRequest(
                        1,
                        10,
                        StatusProdutoPedido.REMOVIDO,
                        false,
                        null,
                        null
                );

        when(pedidoRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(
                BaseException.class,
                () -> pedidoService.decisaoProblema(request)
        );

        verify(pedidoRepository).findById(1);
        verifyNoInteractions(produtoPedidoRepository);
    }

    @Test
    void decisaoProblema_deveLancarExcecao_quandoProdutoPedidoNaoExistir() {
        DecisaoProblemaRequest request =
                new DecisaoProblemaRequest(
                        1,
                        10,
                        StatusProdutoPedido.REMOVIDO,
                        false,
                        null,
                        null
                );

        Pedido pedido = Pedido.builder()
                .id(1)
                .status(StatusPedido.AGUARDANDO_DECISAO)
                .build();

        when(pedidoRepository.findById(1))
                .thenReturn(Optional.of(pedido));

        when(produtoPedidoRepository.findById(10))
                .thenReturn(Optional.empty());

        assertThrows(
                BaseException.class,
                () -> pedidoService.decisaoProblema(request)
        );

        verify(pedidoRepository).findById(1);
        verify(produtoPedidoRepository).findById(10);
    }

    @Test
    void decisaoProblema_deveLancarExcecao_quandoPedidoNaoPermitirDecisao() {
        DecisaoProblemaRequest request =
                new DecisaoProblemaRequest(
                        1,
                        10,
                        StatusProdutoPedido.DISPONIVEL,
                        false,
                        null,
                        null
                );

        Pedido pedido = Pedido.builder()
                .id(1)
                .status(StatusPedido.EM_PREPARO)
                .build();

        ProdutoPedido produtoPedido = ProdutoPedido.builder()
                .id(10)
                .build();

        when(pedidoRepository.findById(1))
                .thenReturn(Optional.of(pedido));

        when(produtoPedidoRepository.findById(10))
                .thenReturn(Optional.of(produtoPedido));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> pedidoService.decisaoProblema(request)
        );

        assertEquals(
                ErrorEnum.PEDIDO_NAO_PERMITE_DECISAO,
                exception.getErrorEnum()
        );

        verify(pedidoRepository).findById(1);
        verify(produtoPedidoRepository).findById(10);
    }

    @Test
    void decisaoProblema_deveCancelarPedidoQuandoPedidoCancelado() {
        DecisaoProblemaRequest request =
                new DecisaoProblemaRequest(
                        1,
                        10,
                        StatusProdutoPedido.DISPONIVEL,
                        true,
                        null,
                        null
                );

        Pedido pedido = Pedido.builder()
                .id(1)
                .status(StatusPedido.AGUARDANDO_DECISAO)
                .build();

        ProdutoPedido produtoPedido = ProdutoPedido.builder()
                .id(10)
                .build();

        when(pedidoRepository.findById(1))
                .thenReturn(Optional.of(pedido));

        when(produtoPedidoRepository.findById(10))
                .thenReturn(Optional.of(produtoPedido));

        pedidoService.decisaoProblema(request);

        assertEquals(
                StatusPedido.CANCELADO,
                pedido.getStatus()
        );

        verify(pedidoRepository).findById(1);
        verify(produtoPedidoRepository).findById(10);
    }

    @Test
    void decisaoProblema_deveRemoverProdutoEEnviarPedidoParaCozinha() {
        DecisaoProblemaRequest request =
                new DecisaoProblemaRequest(
                        1,
                        10,
                        StatusProdutoPedido.REMOVIDO,
                        false,
                        null,
                        null
                );

        Pedido pedido = Pedido.builder()
                .id(1)
                .status(StatusPedido.AGUARDANDO_DECISAO)
                .build();

        ProdutoPedido produtoPedido = ProdutoPedido.builder()
                .id(10)
                .status(StatusProdutoPedido.DISPONIVEL)
                .build();

        when(pedidoRepository.findById(1))
                .thenReturn(Optional.of(pedido));

        when(produtoPedidoRepository.findById(10))
                .thenReturn(Optional.of(produtoPedido));

        when(produtoPedidoRepository.findByIdPedidoAndStatus(
                1,
                StatusProdutoPedido.DISPONIVEL
        )).thenReturn(List.of(produtoPedido));

        pedidoService.decisaoProblema(request);

        assertEquals(
                StatusProdutoPedido.REMOVIDO,
                produtoPedido.getStatus()
        );

        assertEquals(
                StatusPedido.ENVIADO_COZINHA,
                pedido.getStatus()
        );

        verify(produtoPedidoRepository)
                .findByIdPedidoAndStatus(
                        1,
                        StatusProdutoPedido.DISPONIVEL
                );
    }

    @Test
    void decisaoProblema_deveCancelarPedidoQuandoNaoExistiremProdutosDisponiveis() {
        DecisaoProblemaRequest request =
                new DecisaoProblemaRequest(
                        1,
                        10,
                        StatusProdutoPedido.REMOVIDO,
                        false,
                        null,
                        null
                );

        Pedido pedido = Pedido.builder()
                .id(1)
                .status(StatusPedido.AGUARDANDO_DECISAO)
                .build();

        ProdutoPedido produtoPedido = ProdutoPedido.builder()
                .id(10)
                .status(StatusProdutoPedido.DISPONIVEL)
                .build();

        when(pedidoRepository.findById(1))
                .thenReturn(Optional.of(pedido));

        when(produtoPedidoRepository.findById(10))
                .thenReturn(Optional.of(produtoPedido));

        when(produtoPedidoRepository.findByIdPedidoAndStatus(
                1,
                StatusProdutoPedido.DISPONIVEL
        )).thenReturn(Collections.emptyList());

        pedidoService.decisaoProblema(request);

        assertEquals(
                StatusPedido.CANCELADO,
                pedido.getStatus()
        );
    }

    @Test
    void decisaoProblema_deveSubstituirProduto() {
        DecisaoProblemaRequest request =
                new DecisaoProblemaRequest(
                        1,
                        10,
                        StatusProdutoPedido.DISPONIVEL,
                        false,
                        99,
                        "Batata"
                );

        Pedido pedido = Pedido.builder()
                .id(1)
                .status(StatusPedido.AGUARDANDO_DECISAO)
                .build();

        ProdutoPedido produtoPedido = ProdutoPedido.builder()
                .id(10)
                .idProduto(5)
                .build();

        when(pedidoRepository.findById(1))
                .thenReturn(Optional.of(pedido));

        when(produtoPedidoRepository.findById(10))
                .thenReturn(Optional.of(produtoPedido));

        pedidoService.decisaoProblema(request);

        assertEquals(
                99,
                produtoPedido.getIdProduto()
        );

        assertEquals(
                "Batata",
                produtoPedido.getNomeProduto()
        );

        assertEquals(
                StatusPedido.ENVIADO_COZINHA,
                pedido.getStatus()
        );
    }
}
