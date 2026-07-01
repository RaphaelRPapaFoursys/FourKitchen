package br.com.fourkitchen.ms_pedidos.service;

import br.com.fourkitchen.ms_pedidos.dto.request.AlterarPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.request.CriarPedidoRequest;
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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PedidoService {
    private static final Collection<StatusPedido> STATUS_ATIVOS = List.of(
            StatusPedido.ENVIADO_COZINHA,
            StatusPedido.EM_PREPARO,
            StatusPedido.PRONTO,
            StatusPedido.ENTREGUE
    );

    private static final Collection<StatusPedido> STATUS_COZINHA = List.of(
            StatusPedido.ENVIADO_COZINHA,
            StatusPedido.EM_PREPARO,
            StatusPedido.PRONTO
    );

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

        if (pedido.getCodigo() == null) {
            pedido.setCodigo(gerarCodigoPedidoUnico());
        }

        pedido.setStatus(StatusPedido.ENVIADO_COZINHA);

        pedidoRepository.save(pedido);

        if (pedidoRequest.itens() != null) {
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

    public List<PedidoResponse> findPedidosCozinha() {
        return pedidoRepository.findByStatusIn(STATUS_COZINHA)
                .stream()
                .map(pedidoResponseMapper::map)
                .toList();
    }

    public boolean possuiPedidosAtivos(Integer atendimentoId) {
        return pedidoRepository.existsByIdAtendimentoAndStatusIn(atendimentoId, STATUS_ATIVOS);
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

        if(alterarPedidoRequest.idAtendimento() != null) {
            pedido.setIdAtendimento(alterarPedidoRequest.idAtendimento());
        }
        //return pedidoResponseMapper.map(pedido);
    }

   @Transactional
    public void softDelete(Integer id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(PedidoInexistenteException::new);

        pedido.setStatus(StatusPedido.CANCELADO);
    }

    private Integer gerarCodigoPedidoUnico() {
        Integer codigo;

        do {
            codigo = ThreadLocalRandom.current().nextInt(100000, 1000000);
        } while (pedidoRepository.existsByCodigo(codigo));

        return codigo;
    }
}
