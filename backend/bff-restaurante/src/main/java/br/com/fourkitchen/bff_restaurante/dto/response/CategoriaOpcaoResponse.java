package br.com.fourkitchen.bff_restaurante.dto.response;

public record CategoriaOpcaoResponse(
        Integer id,
        String nome,
        Boolean ativo
) {
}
