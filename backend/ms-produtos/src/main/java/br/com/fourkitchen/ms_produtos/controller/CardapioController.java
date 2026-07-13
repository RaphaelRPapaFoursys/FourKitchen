package br.com.fourkitchen.ms_produtos.controller;

import br.com.fourkitchen.ms_produtos.dto.response.CategoriaCardapioResponse;
import br.com.fourkitchen.ms_produtos.dto.response.CategoriaCardapioResumoResponse;
import br.com.fourkitchen.ms_produtos.dto.response.CardapioPaginadoResponse;
import br.com.fourkitchen.ms_produtos.service.CardapioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaCardapioResumoResponse>> buscarCategoriasAtivas() {
        return ResponseEntity.ok(cardapioService.buscarCategoriasAtivas());
    }

    @GetMapping("/paginado")
    public ResponseEntity<CardapioPaginadoResponse> buscarCardapioPaginado(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "12") Integer size,
            @RequestParam(name = "categoriaId", required = false) Integer categoriaId
    ) {
        int pagina = page == null ? 0 : Math.max(0, page);
        int tamanho = size == null ? 12 : Math.min(Math.max(1, size), 30);
        Pageable pageable = PageRequest.of(pagina, tamanho);

        return ResponseEntity.ok(cardapioService.buscarCardapioPaginado(categoriaId, pageable));
    }
}
