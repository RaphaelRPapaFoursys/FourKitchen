package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.SessaoMesaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.CriarPedidoRequest;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ProdutoPedidoRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarPedidoMesaRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.ItemPedidoMesaRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoMesaResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MesaPedidoService {

    private static final String CANAL_MESA = "MESA";

    private static final String STATUS_ENVIADO_COZINHA = "ENVIADO_COZINHA";

    private final MesaClient mesaClient;

    private final PedidoClient pedidoClient;

    public PedidoMesaResponse criarPedido(CriarPedidoMesaRequest request) {
        SessaoMesaResponse sessao = validarSessaoMesa(request);
        PedidoResponse pedido = criarPedidoNoMsPedidos(request, sessao);

        return new PedidoMesaResponse(
                pedido.id(),
                pedido.codigo(),
                pedido.canal(),
                pedido.status(),
                pedido.idMesa(),
                pedido.idAtendimento()
        );
    }

    private SessaoMesaResponse validarSessaoMesa(CriarPedidoMesaRequest request) {
        try {
            return mesaClient.validarSessaoMesa(request.idMesa(), request.codigoSessao());
        } catch (FeignException e) {
            if (e.status() == 400 || e.status() == 404) {
                throw new BaseException(ErrorEnum.SESSAO_MESA_INVALIDA);
            }

            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private PedidoResponse criarPedidoNoMsPedidos(CriarPedidoMesaRequest request, SessaoMesaResponse sessao) {
        try {
            return pedidoClient.criarPedido(new CriarPedidoRequest(
                    null,
                    null,
                    CANAL_MESA,
                    STATUS_ENVIADO_COZINHA,
                    sessao.idMesa(),
                    null,
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

    private List<ProdutoPedidoRequest> mapearItens(List<ItemPedidoMesaRequest> itens) {
        return itens.stream()
                .map(item -> new ProdutoPedidoRequest(
                        item.idProduto(),
                        item.quantidade(),
                        item.precoUnitario(),
                        item.observacao()
                ))
                .toList();
    }
}
