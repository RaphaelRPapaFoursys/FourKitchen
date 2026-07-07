package br.com.fourkitchen.ms_pedidos.controller;

import br.com.fourkitchen.ms_pedidos.dto.request.AlterarProdutoPedidoRequest;
import br.com.fourkitchen.ms_pedidos.dto.response.ProdutoPedidoResponse;
import br.com.fourkitchen.ms_pedidos.exceptions.BaseException;
import br.com.fourkitchen.ms_pedidos.service.ProdutoPedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("itempedido")
public class ProdutoPedidoController {
    @Autowired
    private ProdutoPedidoService produtoPedidoService;

//    @PostMapping
//    private ResponseEntity<Void> criarProdutoPedido(
//            @RequestBody CriarProdutoPedidoRequest criarProdutoPedidoRequest
//    ) {
//        produtoPedidoService.createProdutoPedido(criarProdutoPedidoRequest);
//
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .build();
//    }

    @GetMapping("{id}")
    private ResponseEntity<ProdutoPedidoResponse> buscarProdutoPedido(
            @PathVariable Integer id
    ) {
        ProdutoPedidoResponse produtoPedidoResponse = produtoPedidoService.findById(id);

        try {
            return ResponseEntity
                    .ok()
                    .body(produtoPedidoResponse);
        } catch (BaseException error) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    @GetMapping
    private ResponseEntity<List<ProdutoPedidoResponse>> buscarProdutosPedidos() {
        List<ProdutoPedidoResponse> listaProdutoPedidoResponse = produtoPedidoService.findAll();

        try {
            return ResponseEntity
                    .ok()
                    .body(listaProdutoPedidoResponse);
        } catch (BaseException error) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }

    @PatchMapping("{id}")
    private ResponseEntity<Void> alterarProdutoPedido(
            @RequestBody AlterarProdutoPedidoRequest alterarProdutoPedidoRequest,
            @PathVariable Integer id
    ) {
        produtoPedidoService.patchProdutoPedido(id, alterarProdutoPedidoRequest);

        return ResponseEntity
                .ok()
                .build();
    }
}
