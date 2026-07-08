package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.SessaoMesaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.CriarPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ProdutoPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoMesaRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.ItemPedidoMesaRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.ItemPedidoMesaStatusResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoMesaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoMesaStatusResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MesaPedidoService {

    private static final String CANAL_MESA = "MESA";

    private static final String STATUS_ENVIADO_COZINHA = "ENVIADO_COZINHA";

    private static final String PERFIL_MESA = "MESA";

    private final MesaClient mesaClient;

    private final ProdutoClient produtoClient;

    private final PedidoClient pedidoClient;

    public PedidoMesaResponse criarPedido(CriarPedidoMesaRequest request, Authentication authentication) {
        UsuarioAutenticado usuario = obterUsuarioMesa(authentication);
        SessaoMesaResponse sessao = validarSessaoMesa(usuario.idMesa(), request.codigoSessao());
        List<ProdutoPedidoRequest> itens = mapearItensComPrecoAtual(request.itens());
        PedidoResponse pedido = criarPedidoNoMsPedidos(usuario, sessao, itens);

        return new PedidoMesaResponse(
                pedido.id(),
                pedido.codigo(),
                pedido.canal(),
                pedido.status(),
                pedido.idMesa(),
                pedido.idAtendimento()
        );
    }

    public List<PedidoMesaStatusResponse> listarPedidosDoAtendimentoAtual(
            Integer codigoAtendimento,
            Authentication authentication
    ) {
        UsuarioAutenticado usuario = obterUsuarioMesa(authentication);
        SessaoMesaResponse sessao = validarSessaoMesa(usuario.idMesa(), codigoAtendimento);

        try {
            return pedidoClient.listarPedidosDetalhadosPorAtendimento(sessao.idAtendimento())
                    .stream()
                    .map(pedido -> mapearPedidoStatusMesa(pedido, sessao.codigoSessao()))
                    .toList();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private UsuarioAutenticado obterUsuarioMesa(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        if (!PERFIL_MESA.equals(usuario.perfil()) || usuario.idMesa() == null || usuario.idMesa() <= 0) {
            throw new BaseException(ErrorEnum.ACESSO_NEGADO);
        }

        return usuario;
    }

    private SessaoMesaResponse validarSessaoMesa(Integer idMesa, Integer codigoSessao) {
        if (codigoSessao == null || codigoSessao <= 0) {
            throw new BaseException(ErrorEnum.SESSAO_MESA_INVALIDA);
        }

        try {
            return mesaClient.validarSessaoMesa(idMesa, codigoSessao);
        } catch (FeignException e) {
            if (e.status() == 400 || e.status() == 404) {
                throw new BaseException(ErrorEnum.SESSAO_MESA_INVALIDA);
            }

            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private PedidoMesaStatusResponse mapearPedidoStatusMesa(
            PedidoCozinhaResponse pedido,
            Integer codigoAtendimento
    ) {
        return new PedidoMesaStatusResponse(
                pedido.id(),
                pedido.codigo(),
                pedido.canal(),
                pedido.status(),
                pedido.idMesa(),
                pedido.idAtendimento(),
                codigoAtendimento,
                pedido.dataCriacao(),
                itensDoPedido(pedido).stream()
                        .map(this::mapearItemStatusMesa)
                        .toList()
        );
    }

    private List<ItemPedidoCozinhaResponse> itensDoPedido(PedidoCozinhaResponse pedido) {
        if (pedido.itens() == null) {
            return List.of();
        }

        return pedido.itens();
    }

    private ItemPedidoMesaStatusResponse mapearItemStatusMesa(ItemPedidoCozinhaResponse item) {
        return new ItemPedidoMesaStatusResponse(
                item.idProduto(),
                null,
                item.quantidade(),
                item.observacao()
        );
    }

    private PedidoResponse criarPedidoNoMsPedidos(
            UsuarioAutenticado usuario,
            SessaoMesaResponse sessao,
            List<ProdutoPedidoRequest> itens
    ) {
        try {
            return pedidoClient.criarPedido(new CriarPedidoRequest(
                    null,
                    null,
                    CANAL_MESA,
                    STATUS_ENVIADO_COZINHA,
                    sessao.idMesa(),
                    Math.toIntExact(usuario.id()),
                    sessao.idAtendimento(),
                    itens
            ));
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private List<ProdutoPedidoRequest> mapearItensComPrecoAtual(List<ItemPedidoMesaRequest> itens) {
        return itens.stream()
                .map(this::mapearItemComPrecoAtual)
                .toList();
    }

    private ProdutoPedidoRequest mapearItemComPrecoAtual(ItemPedidoMesaRequest item) {
        ProdutoDisponibilidadeResponse disponibilidade = buscarDisponibilidade(item.idProduto());

        if (disponibilidade == null || !Boolean.TRUE.equals(disponibilidade.disponivel())) {
            throw new BaseException(ErrorEnum.PRODUTO_INDISPONIVEL);
        }

        return new ProdutoPedidoRequest(
                item.idProduto(),
                item.quantidade(),
                disponibilidade.preco(),
                item.observacao()
        );
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
}
