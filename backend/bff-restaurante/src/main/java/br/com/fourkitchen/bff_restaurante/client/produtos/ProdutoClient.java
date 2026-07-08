package br.com.fourkitchen.bff_restaurante.client.produtos;

import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaCardapioClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.AtualizarProdutoClientRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CriarProdutoClientRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoGestorClientResponse;
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
    List<ProdutoGestorClientResponse> listarProdutos();

    @GetMapping("/api/produtos/cardapio")
    List<CategoriaCardapioClientResponse> buscarCardapio();

    @GetMapping("/api/produtos/{id}/disponibilidade")
    ProdutoDisponibilidadeResponse verificarDisponibilidade(@PathVariable Integer id);

    @PostMapping("/api/produtos")
    ProdutoGestorClientResponse criarProduto(@RequestBody CriarProdutoClientRequest request);

    @PutMapping("/api/produtos/{id}")
    ProdutoGestorClientResponse atualizarProduto(
            @PathVariable Integer id,
            @RequestBody AtualizarProdutoClientRequest request
    );

    @PatchMapping("/api/produtos/{id}/ativar")
    ProdutoGestorClientResponse ativarProduto(@PathVariable Integer id);

    @PatchMapping("/api/produtos/{id}/desativar")
    ProdutoGestorClientResponse desativarProduto(@PathVariable Integer id);
}
