package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.AtualizarProdutoRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarCategoriaRequest;
import br.com.fourkitchen.bff_restaurante.dto.request.CriarProdutoRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.CategoriaGestorResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.ProdutoGestorResponse;
import br.com.fourkitchen.bff_restaurante.service.GestorProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gestor")
public class GestorProdutoController {

    private final GestorProdutoService gestorProdutoService;

    @GetMapping("/produtos")
    public ResponseEntity<List<ProdutoGestorResponse>> listarProdutos() {
        return ResponseEntity.ok(gestorProdutoService.listarProdutos());
    }

    @PostMapping("/produtos")
    public ResponseEntity<ProdutoGestorResponse> criarProduto(
            @RequestBody @Valid CriarProdutoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestorProdutoService.criarProduto(request));
    }

    @PutMapping("/produtos/{id}")
    public ResponseEntity<ProdutoGestorResponse> atualizarProduto(
            @PathVariable Integer id,
            @RequestBody @Valid AtualizarProdutoRequest request
    ) {
        return ResponseEntity.ok(gestorProdutoService.atualizarProduto(id, request));
    }

    @PatchMapping("/produtos/{id}/ativar")
    public ResponseEntity<ProdutoGestorResponse> ativarProduto(@PathVariable Integer id) {
        return ResponseEntity.ok(gestorProdutoService.ativarProduto(id));
    }

    @PatchMapping("/produtos/{id}/desativar")
    public ResponseEntity<ProdutoGestorResponse> desativarProduto(@PathVariable Integer id) {
        return ResponseEntity.ok(gestorProdutoService.desativarProduto(id));
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaGestorResponse>> listarCategorias() {
        return ResponseEntity.ok(gestorProdutoService.listarCategorias());
    }

    @PostMapping("/categorias")
    public ResponseEntity<CategoriaGestorResponse> criarCategoria(
            @RequestBody @Valid CriarCategoriaRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestorProdutoService.criarCategoria(request));
    }
}
