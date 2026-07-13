package br.com.fourkitchen.ms_produtos.controller;

import br.com.fourkitchen.ms_produtos.dto.request.AtualizarProdutoRequest;
import br.com.fourkitchen.ms_produtos.dto.request.CriarProdutoRequest;
import br.com.fourkitchen.ms_produtos.dto.response.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.ms_produtos.dto.response.ProdutoImagemResponse;
import br.com.fourkitchen.ms_produtos.dto.response.ProdutoResponse;
import br.com.fourkitchen.ms_produtos.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listarProdutos() {
        return ResponseEntity.ok(produtoService.listarProdutos());
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<ProdutoResponse>> listarProdutosDisponiveis() {
        return ResponseEntity.ok(produtoService.listarProdutosDisponiveis());
    }

    @GetMapping("/{id}/disponibilidade")
    public ResponseEntity<ProdutoDisponibilidadeResponse> verificarDisponibilidadeParaVenda(@PathVariable Integer id) {
        return ResponseEntity.ok(produtoService.verificarDisponibilidadeParaVenda(id));
    }

    @GetMapping("/{id}/imagem")
    public ResponseEntity<byte[]> buscarImagem(@PathVariable Integer id) {
        ProdutoImagemResponse imagem = produtoService.buscarImagem(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagem.contentType()))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .header(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING)
                .body(imagem.bytes());
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criarProduto(
            @RequestBody @Valid CriarProdutoRequest request
    ) {
        ProdutoResponse response = produtoService.criarProduto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizarProduto(
            @PathVariable Integer id,
            @RequestBody @Valid AtualizarProdutoRequest request
    ) {
        return ResponseEntity.ok(produtoService.atualizarProduto(id, request));
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<ProdutoResponse> ativarProduto(@PathVariable Integer id) {
        return ResponseEntity.ok(produtoService.ativarProduto(id));
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<ProdutoResponse> desativarProduto(@PathVariable Integer id) {
        return ResponseEntity.ok(produtoService.desativarProduto(id));
    }
}
