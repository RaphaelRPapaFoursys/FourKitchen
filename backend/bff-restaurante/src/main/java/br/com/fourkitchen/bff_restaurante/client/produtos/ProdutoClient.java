package br.com.fourkitchen.bff_restaurante.client.produtos;

import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaCardapioClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaGestorClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaGestorRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoGestorClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoGestorRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "ms-produtos", url = "${clients.ms-produtos.url}")
public interface ProdutoClient {

    @GetMapping("/api/produtos/cardapio")
    List<CategoriaCardapioClientResponse> buscarCardapio();

    @GetMapping("/api/produtos/{id}/disponibilidade")
    ProdutoDisponibilidadeResponse verificarDisponibilidade(@PathVariable Integer id);

    @GetMapping("/api/produtos")
    List<ProdutoGestorClientResponse> listarProdutos();

    @PostMapping("/api/produtos")
    ProdutoGestorClientResponse criarProduto(@RequestBody ProdutoGestorRequest request);

    @PutMapping("/api/produtos/{id}")
    ProdutoGestorClientResponse atualizarProduto(
            @PathVariable Integer id,
            @RequestBody ProdutoGestorRequest request
    );

    @PatchMapping("/api/produtos/{id}/ativar")
    ProdutoGestorClientResponse ativarProduto(@PathVariable Integer id);

    @PatchMapping("/api/produtos/{id}/desativar")
    ProdutoGestorClientResponse desativarProduto(@PathVariable Integer id);

    @GetMapping("/api/categorias")
    List<CategoriaGestorClientResponse> listarCategorias();

    @PostMapping("/api/categorias")
    CategoriaGestorClientResponse criarCategoria(@RequestBody CategoriaGestorRequest request);

    @PutMapping("/api/categorias/{id}")
    CategoriaGestorClientResponse atualizarCategoria(
            @PathVariable Integer id,
            @RequestBody CategoriaGestorRequest request
    );

    @PatchMapping("/api/categorias/{id}/ativar")
    CategoriaGestorClientResponse ativarCategoria(@PathVariable Integer id);

    @PatchMapping("/api/categorias/{id}/desativar")
    CategoriaGestorClientResponse desativarCategoria(@PathVariable Integer id);
}
