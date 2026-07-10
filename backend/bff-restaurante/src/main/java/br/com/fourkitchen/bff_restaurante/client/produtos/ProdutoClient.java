package br.com.fourkitchen.bff_restaurante.client.produtos;

import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaCardapioClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CriarCategoriaClientRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CriarProdutoClientRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.AtualizarProdutoClientRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoClientResponse;
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

    @GetMapping("/api/produtos")
    List<ProdutoClientResponse> listarProdutos();

    @PostMapping("/api/produtos")
    ProdutoClientResponse criarProduto(@RequestBody CriarProdutoClientRequest request);

    @PutMapping("/api/produtos/{id}")
    ProdutoClientResponse atualizarProduto(
            @PathVariable Integer id,
            @RequestBody AtualizarProdutoClientRequest request
    );

    @PatchMapping("/api/produtos/{id}/ativar")
    ProdutoClientResponse ativarProduto(@PathVariable Integer id);

    @PatchMapping("/api/produtos/{id}/desativar")
    ProdutoClientResponse desativarProduto(@PathVariable Integer id);

    @GetMapping("/api/produtos/cardapio")
    List<CategoriaCardapioClientResponse> buscarCardapio();

    @GetMapping("/api/produtos/{id}/disponibilidade")
    ProdutoDisponibilidadeResponse verificarDisponibilidade(@PathVariable Integer id);

    @GetMapping("/api/categorias")
    List<CategoriaClientResponse> listarCategorias();

    @PostMapping("/api/categorias")
    CategoriaClientResponse criarCategoria(@RequestBody CriarCategoriaClientRequest request);
}
