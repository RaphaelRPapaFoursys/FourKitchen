package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.DecisaoProblemaPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.DecisaoProblemaRequest;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DecisaoProblemaService {

    private final PedidoClient pedidoClient;

    private final ProdutoClient produtoClient;

    public void registrar(DecisaoProblemaRequest request) {
        ProdutoDisponibilidadeResponse produtoSubstituto = buscarProdutoSubstituto(request.idNovoProduto());

        try {
            pedidoClient.decisaoProblema(new DecisaoProblemaPedidoRequest(
                    request.idPedido(),
                    request.idProdutoPedido(),
                    request.novoStatusProdutoPedido(),
                    request.pedidoCancelado(),
                    produtoSubstituto == null ? null : produtoSubstituto.produtoId(),
                    produtoSubstituto == null ? null : produtoSubstituto.nome(),
                    produtoSubstituto == null ? null : produtoSubstituto.preco(),
                    request.observacaoNovoProduto()
            ));
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.PEDIDO_NAO_PERMITE_DECISAO);
            }

            if (e.status() == 404) {
                throw new BaseException(ErrorEnum.PEDIDO_NAO_ENCONTRADO);
            }

            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
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
