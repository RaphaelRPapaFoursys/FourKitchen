package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.*;
import br.com.fourkitchen.bff_restaurante.client.produtos.ProdutoClient;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.dto.DestinoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.EventoPedido;
import br.com.fourkitchen.bff_restaurante.dto.TipoNotificacao;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarNotificacaoRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.DecisaoProblemaRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.ItemFilaCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoFilaCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoStatusCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CozinhaService {

    private final PedidoClient pedidoClient;

    private final ProdutoClient produtoClient;

    private final NotificacaoService notificacaoService;

    public List<PedidoFilaCozinhaResponse> listarFila() {
        try {
            return pedidoClient.listarFilaCozinha()
                    .stream()
                    .map(this::mapearPedido)
                    .toList();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    public PedidoStatusCozinhaResponse iniciarPreparo(Integer id) {
        PedidoResponse pedido = alterarStatus(id, EventoPedido.PEDIDO_EM_PREPARO);
        registrarEvento(EventoPedido.PEDIDO_EM_PREPARO);

        return mapearStatus(pedido);
    }

    public PedidoStatusCozinhaResponse finalizarPreparo(Integer id) {
        PedidoResponse pedido = alterarStatus(id, EventoPedido.PEDIDO_PRONTO);
        registrarEvento(EventoPedido.PEDIDO_PRONTO);

        return mapearStatus(pedido);
    }

    private PedidoResponse alterarStatus(Integer id, EventoPedido eventoPedido) {
        try {
            if (EventoPedido.PEDIDO_EM_PREPARO.equals(eventoPedido)) {
                return pedidoClient.iniciarPreparo(id);
            }

            return pedidoClient.finalizarPreparo(id);
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

    private void registrarEvento(EventoPedido eventoPedido) {
        notificacaoService.criarNotificacao(new CriarNotificacaoRequest(
                eventoPedido.tipoNotificacao(),
                eventoPedido.destino(),
                null,
                null,
                null
        ));
    }

    private PedidoFilaCozinhaResponse mapearPedido(PedidoCozinhaResponse pedido) {
        return new PedidoFilaCozinhaResponse(
                pedido.id(),
                pedido.codigo(),
                pedido.canal(),
                pedido.status(),
                pedido.idMesa(),
                pedido.idAtendimento(),
                pedido.dataCriacao(),
                itensDoPedido(pedido).stream()
                        .map(this::mapearItem)
                        .toList()
        );
    }

    private List<ItemPedidoCozinhaResponse> itensDoPedido(PedidoCozinhaResponse pedido) {
        if (pedido.itens() == null) {
            return List.of();
        }

        return pedido.itens();
    }

    private ItemFilaCozinhaResponse mapearItem(ItemPedidoCozinhaResponse item) {
        return new ItemFilaCozinhaResponse(
                item.id(),
                item.idProduto(),
                item.quantidade(),
                item.precoUnitario(),
                item.observacao()
        );
    }

    private PedidoStatusCozinhaResponse mapearStatus(PedidoResponse pedido) {
        return new PedidoStatusCozinhaResponse(
                pedido.id(),
                pedido.codigo(),
                pedido.canal(),
                pedido.status(),
                pedido.idMesa(),
                pedido.idAtendimento()
        );
    }

    public SinalizarProblemaResponse sinalizarProblema(SinalizarProblemaRequest request) {
        try {
            SinalizarProblemaResponse response = pedidoClient.sinalizarProblema(request);

            registrarEvento(EventoPedido.PEDIDO_COM_PROBLEMA);

            return response;
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new BaseException(ErrorEnum.PEDIDO_NAO_ENCONTRADO);
            }

            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    public void decisaoProblema(DecisaoProblemaRequest decisaoProblemaRequest) {
        try {
            if(decisaoProblemaRequest.idNovoProduto() != null) {
                ProdutoDisponibilidadeResponse produtoDisponibilidadeResponse = produtoClient.verificarDisponibilidade(decisaoProblemaRequest.idNovoProduto());

                if(!Boolean.TRUE.equals(produtoDisponibilidadeResponse.disponivel())) {
                    throw new BaseException(ErrorEnum.PRODUTO_INDISPONIVEL);
                }
            }

            pedidoClient.decisaoProblema(decisaoProblemaRequest);
        } catch (FeignException error) {
            if(error.status() == 400) {
                throw new BaseException(ErrorEnum.PEDIDO_NAO_PERMITE_DECISAO);
            }
        }
    }
}
