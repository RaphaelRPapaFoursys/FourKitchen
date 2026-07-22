package br.com.fourkitchen.ms_produtos.dto.response;

public record CategoriaOpcaoResponse(
        Integer id,
        String nome,
        Boolean ativo
) {
}
