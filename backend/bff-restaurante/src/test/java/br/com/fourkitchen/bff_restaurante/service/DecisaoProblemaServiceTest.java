package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.DecisaoProblemaPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.DecisaoProblemaRequest;
import br.com.fourkitchen.bff_restaurante.enums.StatusProdutoPedido;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisaoProblemaServiceTest {

    @Mock
    private PedidoClient pedidoClient;

    @Mock
    private ProdutoClient produtoClient;

    @InjectMocks
    private DecisaoProblemaService decisaoProblemaService;

    @Test
    void registrarDeveEnviarNomeEPrecoDoProdutoSubstituto() {
        DecisaoProblemaRequest request = criarRequest(12);
        when(produtoClient.verificarDisponibilidade(12)).thenReturn(new ProdutoDisponibilidadeResponse(
                12,
                "Coxinha de frango",
                true,
                new BigDecimal("10.50")
        ));

        decisaoProblemaService.registrar(request);

        verify(pedidoClient).decisaoProblema(new DecisaoProblemaPedidoRequest(
                25,
                80,
                StatusProdutoPedido.DISPONIVEL,
                false,
                12,
                "Coxinha de frango",
                new BigDecimal("10.50")
        ));
    }

    @Test
    void registrarDeveRecusarProdutoSubstitutoIndisponivel() {
        DecisaoProblemaRequest request = criarRequest(12);
        when(produtoClient.verificarDisponibilidade(12)).thenReturn(new ProdutoDisponibilidadeResponse(
                12,
                "Coxinha de frango",
                false,
                new BigDecimal("10.50")
        ));

        BaseException exception = assertThrows(
                BaseException.class,
                () -> decisaoProblemaService.registrar(request)
        );

        assertEquals(ErrorEnum.PRODUTO_INDISPONIVEL, exception.getErrorEnum());
        verify(pedidoClient, never()).decisaoProblema(org.mockito.ArgumentMatchers.any());
    }

    private DecisaoProblemaRequest criarRequest(Integer idNovoProduto) {
        return new DecisaoProblemaRequest(
                25,
                80,
                StatusProdutoPedido.DISPONIVEL,
                false,
                idNovoProduto
        );
    }
}
