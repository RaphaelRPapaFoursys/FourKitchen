package br.com.fourkitchen.ms_produtos.controller;

import br.com.fourkitchen.ms_produtos.dto.response.CategoriaCardapioResponse;
import br.com.fourkitchen.ms_produtos.service.CardapioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/produtos/cardapio")
public class CardapioController {

    private final CardapioService cardapioService;

    @GetMapping
    public ResponseEntity<List<CategoriaCardapioResponse>> buscarCardapioMesa() {
        // O BFF deve expor as rotas de canal, como /api/totem/cardapio e
        // /api/mesa/cardapio, consumindo este endpoint unico do ms-produtos.
        return ResponseEntity.ok(cardapioService.buscarCardapio());
    }
}
