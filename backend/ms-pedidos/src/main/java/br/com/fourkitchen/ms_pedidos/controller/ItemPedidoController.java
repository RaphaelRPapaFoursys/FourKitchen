package br.com.fourkitchen.ms_pedidos.controller;

import br.com.fourkitchen.ms_pedidos.dto.response.ItemPedidoResponse;
import br.com.fourkitchen.ms_pedidos.exceptions.ItemPedidoInexistenteException;
import br.com.fourkitchen.ms_pedidos.service.ItemPedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("itempedido")
public class ItemPedidoController {
    @Autowired
    private ItemPedidoService itemPedidoService;

    @GetMapping("{id}")
    private ResponseEntity<ItemPedidoResponse> buscarItemPedido(
            @PathVariable Integer id
    ) {
        ItemPedidoResponse itemPedidoResponse = itemPedidoService.findById(id);

        try {
            return ResponseEntity
                    .ok()
                    .body(itemPedidoResponse);
        } catch (ItemPedidoInexistenteException error) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    @GetMapping()
    private ResponseEntity<List<ItemPedidoResponse>> buscarItensPedidos() {
        List<ItemPedidoResponse> listaItemPedidoResponse = itemPedidoService.findAll();

        try {
            return ResponseEntity
                    .ok()
                    .body(listaItemPedidoResponse);
        } catch (ItemPedidoInexistenteException error) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }
}
