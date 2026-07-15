package br.com.fourkitchen.ms_pedidos.service;

import br.com.fourkitchen.ms_pedidos.dto.request.AlterarProdutoPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.CriarProdutoPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.response.ProdutoPedidoResponse;
import br.com.fourkitchen.ms_pedidos.entities.ProdutoPedido;
import br.com.fourkitchen.ms_pedidos.exceptions.BaseException;
import br.com.fourkitchen.ms_pedidos.exceptions.ErrorEnum;
import br.com.fourkitchen.ms_pedidos.mapper.CriarProdutoPedidoRequestMapper;
import br.com.fourkitchen.ms_pedidos.mapper.ProdutoPedidoResponseMapper;
import br.com.fourkitchen.ms_pedidos.repository.ProdutoPedidoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoPedidoService {
    @Autowired
    private ProdutoPedidoRepository produtoPedidoRepository;

    @Autowired
    private ProdutoPedidoResponseMapper produtoPedidoResponseMapper;

    @Autowired
    private CriarProdutoPedidoRequestMapper criarProdutoPedidoRequestMapper;

    @Transactional
    public void createProdutoPedido(CriarProdutoPedidoRequest produtoPedidoRequest) {
        ProdutoPedido produtoPedido = criarProdutoPedidoRequestMapper.map(produtoPedidoRequest);

        produtoPedidoRepository.save(produtoPedido);
    }

    public ProdutoPedidoResponse findById(Integer id) {
        ProdutoPedido itemPedido = produtoPedidoRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorEnum.PRODUTO_PEDIDO_NAO_ENCONTRADO));

        return produtoPedidoResponseMapper.map(itemPedido);
    }

    public List<ProdutoPedidoResponse> findAll() {
        List<ProdutoPedidoResponse> listaItemPedido = produtoPedidoRepository.findAll()
                .stream().map(produtoPedidoResponseMapper::map)
                .toList();

        return listaItemPedido;
    }

    @Transactional
    public ProdutoPedidoResponse patchProdutoPedido(Integer id, AlterarProdutoPedidoRequest produtoPedidoRequest) {
        ProdutoPedido produtoPedido = produtoPedidoRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorEnum.PRODUTO_PEDIDO_NAO_ENCONTRADO));

        if(produtoPedidoRequest.quantidade() != null) {
            produtoPedido.setQuantidade(produtoPedidoRequest.quantidade());
        }

        if(produtoPedidoRequest.precoUnitario() != null) {
            produtoPedido.setPrecoUnitario(produtoPedidoRequest.precoUnitario());
        }

        if(produtoPedidoRequest.observacao() != null) {
            produtoPedido.setObservacao(produtoPedidoRequest.observacao());
        }

        return produtoPedidoResponseMapper.map(produtoPedido);
    }
}
