package br.com.fourkitchen.bff_restaurante.client.produtos;

import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoDisponibilidadeResponse;
import br.com.fourkitchen.bff_restaurante.client.produtos.dto.ProdutoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ms-produtos", url = "${clients.ms-produtos.url}")
public interface ProdutoClient {

    @GetMapping("/api/produtos")
    List<ProdutoResponse> listarProdutos();

    @GetMapping("/api/produtos/{id}/disponibilidade")
    ProdutoDisponibilidadeResponse verificarDisponibilidade(@PathVariable Integer id);
}
