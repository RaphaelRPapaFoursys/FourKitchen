package br.com.fourkitchen.bff_restaurante.client.produtos;

import br.com.fourkitchen.bff_restaurante.client.produtos.dto.AtualizarCategoriaClientRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaGestorClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CriarCategoriaClientRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "ms-produtos-categorias", url = "${clients.ms-produtos.url}")
public interface CategoriaClient {

    @GetMapping("/api/categorias")
    List<CategoriaGestorClientResponse> listarCategorias();

    @PostMapping("/api/categorias")
    CategoriaGestorClientResponse criarCategoria(@RequestBody CriarCategoriaClientRequest request);

    @PutMapping("/api/categorias/{id}")
    CategoriaGestorClientResponse atualizarCategoria(
            @PathVariable Integer id,
            @RequestBody AtualizarCategoriaClientRequest request
    );

    @PatchMapping("/api/categorias/{id}/ativar")
    CategoriaGestorClientResponse ativarCategoria(@PathVariable Integer id);

    @PatchMapping("/api/categorias/{id}/desativar")
    CategoriaGestorClientResponse desativarCategoria(@PathVariable Integer id);
}
