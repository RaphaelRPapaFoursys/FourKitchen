package br.com.fourkitchen.ms_produtos.controller;

import br.com.fourkitchen.ms_produtos.dto.request.AtualizarCategoriaRequest;
import br.com.fourkitchen.ms_produtos.dto.request.CriarCategoriaRequest;
import br.com.fourkitchen.ms_produtos.dto.response.CategoriaResponse;
import br.com.fourkitchen.ms_produtos.dto.response.CategoriaImagemResponse;
import br.com.fourkitchen.ms_produtos.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
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
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listarCategorias());
    }

    @GetMapping("/{id}/imagem")
    public ResponseEntity<byte[]> buscarImagem(@PathVariable Integer id) {
        CategoriaImagemResponse imagem = categoriaService.buscarImagem(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagem.contentType()))
                .cacheControl(CacheControl.maxAge(12, TimeUnit.HOURS).cachePublic())
                .header(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING)
                .body(imagem.bytes());
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criarCategoria(
            @RequestBody @Valid CriarCategoriaRequest request
    ) {
        CategoriaResponse response = categoriaService.criarCategoria(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> atualizarCategoria(
            @PathVariable Integer id,
            @RequestBody @Valid AtualizarCategoriaRequest request
    ) {
        return ResponseEntity.ok(categoriaService.atualizarCategoria(id, request));
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<CategoriaResponse> ativarCategoria(@PathVariable Integer id) {
        return ResponseEntity.ok(categoriaService.ativarCategoria(id));
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<CategoriaResponse> desativarCategoria(@PathVariable Integer id) {
        return ResponseEntity.ok(categoriaService.desativarCategoria(id));
    }
}
