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
import br.com.fourkitchen.bff_restaurante.realtime.RealtimeEventType;
import br.com.fourkitchen.bff_restaurante.realtime.RealtimeNotifier;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TotemPedidoService {

    private static final String CANAL_TOTEM = "TOTEM";

    private static final String STATUS_ENVIADO_COZINHA = "ENVIADO_COZINHA";

    private static final String PERFIL_TOTEM = "TOTEM";

    private final ProdutoClient produtoClient;

    private final PedidoClient pedidoClient;

    private final ItemPedidoTotemRequestMapper itemPedidoTotemRequestMapper;

    private final PedidoTotemResponseMapper pedidoTotemResponseMapper;

    private final RealtimeNotifier realtimeNotifier;

    public PedidoTotemResponse criarPedido(CriarPedidoTotemRequest request, Authentication authentication) {
        UsuarioAutenticado usuario = obterUsuarioAutenticado(authentication);
        List<ProdutoPedidoRequest> itens = mapearItensComPrecoAtual(request.itens());
        PedidoResponse pedido = criarPedidoNoMsPedidos(usuario, itens);
        realtimeNotifier.pedidoAlterado(
                RealtimeEventType.PEDIDO_CRIADO,
                pedido.id(),
                pedido.idMesa(),
                pedido.idAtendimento(),
                pedido.status()
        );

        return pedidoTotemResponseMapper.map(pedido);
    }

    private UsuarioAutenticado obterUsuarioAutenticado(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        if (!PERFIL_TOTEM.equals(usuario.perfil())) {
            throw new BaseException(ErrorEnum.ACESSO_NEGADO);
        }

        return usuario;
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

    private PedidoResponse criarPedidoNoMsPedidos(UsuarioAutenticado usuario, List<ProdutoPedidoRequest> itens) {
        try {
            return pedidoClient.criarPedido(new CriarPedidoRequest(
                    null,
                    null,
                    CANAL_TOTEM,
                    STATUS_ENVIADO_COZINHA,
                    null,
                    Math.toIntExact(usuario.id()),
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
