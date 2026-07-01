package br.com.fourkitchen.ms_pedidos.service;

import br.com.fourkitchen.ms_pedidos.dto.request.AlterarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.CriarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.CriarProdutoPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.ProdutoPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.response.PedidoResponse;
import br.com.fourkitchen.ms_pedidos.entities.Pedido;
import br.com.fourkitchen.ms_pedidos.entities.ProdutoPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import br.com.fourkitchen.ms_pedidos.exceptions.PedidoInexistenteException;
import br.com.fourkitchen.ms_pedidos.mapper.CriarPedidoRequestMapper;
import br.com.fourkitchen.ms_pedidos.mapper.PedidoResponseMapper;
import br.com.fourkitchen.ms_pedidos.repository.PedidoRepository;
import br.com.fourkitchen.ms_pedidos.repository.ProdutoPedidoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {
    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoPedidoRepository produtoPedidoRepository;

    @Autowired
    private PedidoResponseMapper pedidoResponseMapper;

    @Autowired
    private CriarPedidoRequestMapper criarPedidoRequestMapper;

    public PedidoResponse createPedido(CriarPedidoRequest pedidoRequest) {
        Pedido pedido = criarPedidoRequestMapper.map(pedidoRequest);

        pedido.setStatus(StatusPedido.ENVIADO_COZINHA);

        pedidoRepository.save(pedido);

        for(ProdutoPedidoRequest item : pedidoRequest.itens()) {
            ProdutoPedido produtoPedido = ProdutoPedido
                    .builder()
                    .quantidade(item.quantidade())
                    .idPedido(pedido.getId())
                    .idProduto(item.idProduto())
                    .precoUnitario(item.precoUnitario())
                    .observacao(item.observacao())
                    .build();

            produtoPedidoRepository.save(produtoPedido);
        }

        return pedidoResponseMapper.map(pedido);
    }

    public PedidoResponse findById(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)

                .orElseThrow(PedidoInexistenteException::new);

        return pedidoResponseMapper.map(pedido);
    }

    public List<PedidoResponse> findAll() {
        List<PedidoResponse> listaPedidos = pedidoRepository.findAll()
                .stream()
                .map(pedidoResponseMapper::map)
                .toList();

        return listaPedidos;
    }

    @Transactional
    public void patchPedido(Integer id, AlterarPedidoRequest alterarPedidoRequest) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(PedidoInexistenteException::new);

        if(alterarPedidoRequest.canal() != null) {
            pedido.setCanal(alterarPedidoRequest.canal());
        }

        if(alterarPedidoRequest.status() != null) {
            pedido.setStatus(alterarPedidoRequest.status());
        }

        if(alterarPedidoRequest.idMesa() != null) {
            pedido.setIdMesa(alterarPedidoRequest.idMesa());
        }

        if(alterarPedidoRequest.idUsuario() != null) {
            pedido.setIdUsuario(alterarPedidoRequest.idUsuario());
        }
        //return pedidoResponseMapper.map(pedido);
    }

   @Transactional
    public void softDelete(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(PedidoInexistenteException::new);

        pedido.setStatus(StatusPedido.CANCELADO);
    }
}
