package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.SessaoMesaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.CriarPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ProdutoPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.ItemPedidoGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoGarcomResponse;
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
public class GarcomPedidoService {

    private static final String CANAL_GARCOM = "GARCOM";

    private static final String STATUS_ENVIADO_COZINHA = "ENVIADO_COZINHA";

    private final MesaClient mesaClient;

    private final ProdutoClient produtoClient;

    private final PedidoClient pedidoClient;

    public PedidoGarcomResponse criarPedido(CriarPedidoGarcomRequest request, Authentication authentication) {
        Integer idGarcom = extrairIdGarcom(authentication);
        SessaoMesaResponse sessao = validarMesaDoGarcom(request.idMesa(), idGarcom);
        PedidoResponse pedido = criarPedidoNoMsPedidos(request, sessao, idGarcom);

        return new PedidoGarcomResponse(
                pedido.id(),
                pedido.codigo(),
                pedido.canal(),
                pedido.status(),
                pedido.idMesa(),
                pedido.idUsuario(),
                pedido.idAtendimento()
        );
    }

    private Integer extrairIdGarcom(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuarioAutenticado)) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        if (usuarioAutenticado.id() == null) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        try {
            return Math.toIntExact(usuarioAutenticado.id());
        } catch (ArithmeticException e) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
    }

    private SessaoMesaResponse validarMesaDoGarcom(Integer idMesa, Integer idGarcom) {
        try {
            return mesaClient.validarMesaAtribuidaGarcom(idMesa, idGarcom);
        } catch (FeignException e) {
            if (e.status() == 403) {
                throw new BaseException(ErrorEnum.MESA_NAO_ATRIBUIDA_AO_GARCOM);
            }

            if (e.status() == 400 || e.status() == 404) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private PedidoResponse criarPedidoNoMsPedidos(
            CriarPedidoGarcomRequest request,
            SessaoMesaResponse sessao,
            Integer idGarcom
    ) {
        try {
            return pedidoClient.criarPedido(new CriarPedidoRequest(
                    null,
                    null,
                    CANAL_GARCOM,
                    STATUS_ENVIADO_COZINHA,
                    sessao.idMesa(),
                    idGarcom,
                    sessao.idAtendimento(),
                    mapearItens(request.itens())
            ));
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private List<ProdutoPedidoRequest> mapearItens(List<ItemPedidoGarcomRequest> itens) {
        return itens.stream()
                .map(this::mapearItem)
                .toList();
    }

    private ProdutoPedidoRequest mapearItem(ItemPedidoGarcomRequest item) {
        ProdutoDisponibilidadeResponse disponibilidade = buscarDisponibilidade(item.idProduto());

        if (disponibilidade == null || !Boolean.TRUE.equals(disponibilidade.disponivel())) {
            throw new BaseException(ErrorEnum.PRODUTO_INDISPONIVEL);
        }

        return new ProdutoPedidoRequest(
                item.idProduto(),
                disponibilidade.nome(),
                item.quantidade(),
                item.precoUnitario(),
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
