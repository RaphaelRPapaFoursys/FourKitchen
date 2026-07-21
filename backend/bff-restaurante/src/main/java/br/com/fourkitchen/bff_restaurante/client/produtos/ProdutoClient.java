package br.com.fourkitchen.bff_restaurante.client.produtos;

import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaCardapioClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaCardapioResumoClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CardapioPaginadoClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaGestorClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaGestorRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoGestorClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoGestorRequest;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoGestorPaginadoClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaGestorPaginadaClientResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.CategoriaOpcaoClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "ms-produtos", url = "${clients.ms-produtos.url}")
public interface ProdutoClient {

    @GetMapping("/api/produtos/cardapio")
    List<CategoriaCardapioClientResponse> buscarCardapio();

    @GetMapping("/api/produtos/cardapio/categorias")
    List<CategoriaCardapioResumoClientResponse> buscarCategoriasAtivasCardapio();

    @GetMapping("/api/produtos/cardapio/paginado")
    CardapioPaginadoClientResponse buscarCardapioPaginado(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size,
            @RequestParam("categoriaId") Integer categoriaId
    );

    @GetMapping("/api/produtos/{id}/disponibilidade")
    ProdutoDisponibilidadeResponse verificarDisponibilidade(@PathVariable Integer id);

    @GetMapping("/api/produtos/{id}/imagem")
    ResponseEntity<byte[]> buscarImagem(@PathVariable Integer id);

    @GetMapping("/api/categorias/{id}/imagem")
    ResponseEntity<byte[]> buscarImagemCategoria(@PathVariable Integer id);

    @GetMapping("/api/produtos")
    ProdutoGestorPaginadoClientResponse listarProdutos(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size,
            @RequestParam(value = "busca", required = false) String busca
    );

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
    CategoriaGestorPaginadaClientResponse listarCategorias(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size,
            @RequestParam(value = "busca", required = false) String busca
    );

    @GetMapping("/api/categorias/opcoes")
    List<CategoriaOpcaoClientResponse> listarOpcoesCategorias();

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
