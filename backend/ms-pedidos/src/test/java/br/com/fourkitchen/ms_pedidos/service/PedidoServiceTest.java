package br.com.fourkitchen.ms_pedidos.service;

import br.com.fourkitchen.ms_pedidos.dto.request.CriarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.ProdutoPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.response.PedidoResponse;
import br.com.fourkitchen.ms_pedidos.entities.Pedido;
import br.com.fourkitchen.ms_pedidos.entities.ProdutoPedido;
import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import br.com.fourkitchen.ms_pedidos.mapper.CriarPedidoRequestMapper;
import br.com.fourkitchen.ms_pedidos.mapper.PedidoResponseMapper;
import br.com.fourkitchen.ms_pedidos.repository.PedidoRepository;
import br.com.fourkitchen.ms_pedidos.repository.ProdutoPedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
        ProdutoPedidoRequest item = new ProdutoPedidoRequest(10, 2, new BigDecimal("29.90"), "Sem cebola");
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

    private Collection<StatusPedido> anyStatusCollection() {
        return any();
    }
}
