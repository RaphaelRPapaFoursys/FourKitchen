package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.CriarPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ProdutoPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoTotemRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.ItemPedidoTotemRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoTotemResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.mapper.ItemPedidoTotemMapperSource;
import br.com.fourkitchen.bff_restaurante.mapper.ItemPedidoTotemRequestMapper;
import br.com.fourkitchen.bff_restaurante.mapper.PedidoTotemResponseMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TotemPedidoService {

    private static final String CANAL_TOTEM = "TOTEM";

    private static final String STATUS_ENVIADO_COZINHA = "ENVIADO_COZINHA";

    private final ProdutoClient produtoClient;

    private final PedidoClient pedidoClient;

    private final ItemPedidoTotemRequestMapper itemPedidoTotemRequestMapper;

    private final PedidoTotemResponseMapper pedidoTotemResponseMapper;

    public PedidoTotemResponse criarPedido(CriarPedidoTotemRequest request) {
        List<ProdutoPedidoRequest> itens = mapearItensComPrecoAtual(request.itens());
        PedidoResponse pedido = criarPedidoNoMsPedidos(itens);

        return pedidoTotemResponseMapper.map(pedido);
    }

    private ProdutoDisponibilidadeResponse buscarDisponibilidade(Integer idProduto) {
        try {
            return produtoClient.verificarDisponibilidade(idProduto);
        } catch (FeignException e) {
            if (e.status() == 400 || e.status() == 404) {
                throw new BaseException(ErrorEnum.PRODUTO_INDISPONIVEL);
            }

            throw new BaseException(ErrorEnum.MS_PRODUTOS_INDISPONIVEL);
        }
    }

    private PedidoResponse criarPedidoNoMsPedidos(List<ProdutoPedidoRequest> itens) {
        try {
            return pedidoClient.criarPedido(new CriarPedidoRequest(
                    null,
                    null,
                    CANAL_TOTEM,
                    STATUS_ENVIADO_COZINHA,
                    null,
                    null,
                    null,
                    itens
            ));
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private List<ProdutoPedidoRequest> mapearItensComPrecoAtual(List<ItemPedidoTotemRequest> itens) {
        return itens.stream()
                .map(this::mapearItemComPrecoAtual)
                .toList();
    }

    private ProdutoPedidoRequest mapearItemComPrecoAtual(ItemPedidoTotemRequest item) {
        ProdutoDisponibilidadeResponse disponibilidade = buscarDisponibilidade(item.idProduto());

        if (disponibilidade == null || !Boolean.TRUE.equals(disponibilidade.disponivel())) {
            throw new BaseException(ErrorEnum.PRODUTO_INDISPONIVEL);
        }

        return itemPedidoTotemRequestMapper.map(new ItemPedidoTotemMapperSource(item, disponibilidade));
    }
}
