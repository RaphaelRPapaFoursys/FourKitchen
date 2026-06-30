package br.com.fourkitchen.ms_pedidos.service;

import br.com.fourkitchen.ms_pedidos.dto.response.ItemPedidoResponse;
import br.com.fourkitchen.ms_pedidos.entities.ItemPedido;
import br.com.fourkitchen.ms_pedidos.exceptions.ItemPedidoInexistenteException;
import br.com.fourkitchen.ms_pedidos.mapper.ItemPedidoResponseMapper;
import br.com.fourkitchen.ms_pedidos.repository.ItemPedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
public class ItemPedidoService {
    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private ItemPedidoResponseMapper itemPedidoResponseMapper;

    public ItemPedidoResponse findById(Integer id) {
        ItemPedido itemPedido = itemPedidoRepository.findById(id)
                .orElseThrow(ItemPedidoInexistenteException::new);

        return itemPedidoResponseMapper.map(itemPedido);
    }

    public List<ItemPedidoResponse> findAll() {
        List<ItemPedidoResponse> listaItemPedido = itemPedidoRepository.findAll()
                .stream().map(itemPedidoResponseMapper::map)
                .toList();

        return listaItemPedido;
    }
}
