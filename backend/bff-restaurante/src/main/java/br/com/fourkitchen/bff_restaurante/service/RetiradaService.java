package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoRetiradaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoBalcaoResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoPainelRetiradaResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RetiradaService {

    private final PedidoClient pedidoClient;

    public List<PedidoPainelRetiradaResponse> listarPainelPublico() {
        return listarFila().stream()
                .map(pedido -> new PedidoPainelRetiradaResponse(pedido.codigo(), pedido.status()))
                .toList();
    }

    public List<PedidoBalcaoResponse> listarFilaBalcao() {
        return listarFila().stream()
                .map(this::mapearPedido)
                .toList();
    }

    public PedidoBalcaoResponse entregar(Integer id) {
        try {
            PedidoResponse pedido = pedidoClient.entregarPedidoTotem(id);
            return new PedidoBalcaoResponse(
                    pedido.id(),
                    pedido.codigo(),
                    pedido.status(),
                    null,
                    null,
                    null
            );
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new BaseException(ErrorEnum.PEDIDO_NAO_ENCONTRADO);
            }
            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.TRANSICAO_STATUS_INVALIDA);
            }
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private List<PedidoRetiradaResponse> listarFila() {
        try {
            return pedidoClient.listarFilaRetiradaTotem();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private PedidoBalcaoResponse mapearPedido(PedidoRetiradaResponse pedido) {
        return new PedidoBalcaoResponse(
                pedido.id(),
                pedido.codigo(),
                pedido.status(),
                pedido.dataCriacao(),
                pedido.dataInicioPreparo(),
                pedido.dataPronto()
        );
    }
}
