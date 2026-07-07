package br.com.fourkitchen.ms_produtos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CriarProdutoRequest(
        @NotBlank(message = "Nome e obrigatorio.")
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres.")
        String nome,

        @Size(max = 255, message = "Descricao deve ter no maximo 255 caracteres.")
        String descricao,

        @NotNull(message = "Preco e obrigatorio.")
        @DecimalMin(value = "0.01", message = "Preco deve ser maior que zero.")
        BigDecimal preco,

        @NotNull(message = "Categoria e obrigatoria.")
        Integer categoriaId
) {
}
