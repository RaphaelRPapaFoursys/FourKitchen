package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.DecisaoProblemaPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.DecisaoProblemaRequest;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.realtime.RealtimeEventType;
import br.com.fourkitchen.bff_restaurante.realtime.RealtimeNotifier;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DecisaoProblemaService {

    private final PedidoClient pedidoClient;

    private final ProdutoClient produtoClient;

    private final RealtimeNotifier realtimeNotifier;

    public void registrar(DecisaoProblemaRequest request) {
        DecisaoProblemaPedidoRequest decisao = montarDecisao(request);
        ProdutoDisponibilidadeResponse produtoSubstituto = buscarProdutoSubstituto(request.idNovoProduto());

        try {
            pedidoClient.decisaoProblema(decisao);
        } catch (FeignException e) {
            tratarErroDecisao(e);
        }
    }

    public void registrarProblemaTotem(DecisaoProblemaRequest request, Integer idGarcom) {
        DecisaoProblemaPedidoRequest decisao = montarDecisao(request);
        ProdutoDisponibilidadeResponse produtoSubstituto = buscarProdutoSubstituto(request.idNovoProduto());

        try {
            var pedido = pedidoClient.decisaoProblema(new DecisaoProblemaPedidoRequest(
                    request.idPedido(),
                    request.idProdutoPedido(),
                    request.novoStatusProdutoPedido(),
                    request.pedidoCancelado(),
                    produtoSubstituto == null ? null : produtoSubstituto.produtoId(),
                    produtoSubstituto == null ? null : produtoSubstituto.nome(),
                    produtoSubstituto == null ? null : produtoSubstituto.preco(),
                    request.observacaoNovoProduto()
            ));
            realtimeNotifier.pedidoAlterado(
                    Boolean.TRUE.equals(request.pedidoCancelado())
                            ? RealtimeEventType.PEDIDO_CANCELADO
                            : RealtimeEventType.PEDIDO_PROBLEMA_RESOLVIDO,
                    pedido.id(),
                    pedido.idMesa(),
                    pedido.idAtendimento(),
                    pedido.status()
            );
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.PEDIDO_NAO_PERMITE_DECISAO);
            }
            pedidoClient.decisaoProblemaTotem(idGarcom, decisao);
        }
    }

    private DecisaoProblemaPedidoRequest montarDecisao(DecisaoProblemaRequest request) {
        ProdutoDisponibilidadeResponse produtoSubstituto = buscarProdutoSubstituto(request.idNovoProduto());
        return new DecisaoProblemaPedidoRequest(
                request.idPedido(),
                request.idProdutoPedido(),
                request.novoStatusProdutoPedido(),
                request.pedidoCancelado(),
                produtoSubstituto == null ? null : produtoSubstituto.produtoId(),
                produtoSubstituto == null ? null : produtoSubstituto.nome(),
                produtoSubstituto == null ? null : produtoSubstituto.preco(),
                request.observacaoNovoProduto()
        );
    }

    private void tratarErroDecisao(FeignException e) {
        if (e.status() == 400) {
            throw new BaseException(ErrorEnum.PEDIDO_NAO_PERMITE_DECISAO);
        }

        if (e.status() == 404) {
            throw new BaseException(ErrorEnum.PEDIDO_NAO_ENCONTRADO);
        }

        throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
    }

    private ProdutoDisponibilidadeResponse buscarProdutoSubstituto(Integer idProduto) {
        if (idProduto == null) {
            return null;
        }

        try {
            ProdutoDisponibilidadeResponse produto = produtoClient.verificarDisponibilidade(idProduto);

            if (produto == null || !Boolean.TRUE.equals(produto.disponivel())) {
                throw new BaseException(ErrorEnum.PRODUTO_INDISPONIVEL);
            }

            return produto;
        } catch (FeignException e) {
            if (e.status() == 400 || e.status() == 404) {
                throw new BaseException(ErrorEnum.PRODUTO_INDISPONIVEL);
            }

            throw new BaseException(ErrorEnum.MS_PRODUTOS_INDISPONIVEL);
        }
    }
}
