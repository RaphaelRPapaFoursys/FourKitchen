package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.request.CriarCategoriaRequest;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CriarCategoriaRequestMapper implements Mapper<CriarCategoriaRequest, Categoria> {

    private final ImagemBase64Mapper imagemBase64Mapper;

    public CriarCategoriaRequestMapper(ImagemBase64Mapper imagemBase64Mapper) {
        this.imagemBase64Mapper = imagemBase64Mapper;
    }

    @Override
    public Categoria map(CriarCategoriaRequest source) {
        byte[] imagem = imagemBase64Mapper.paraBytes(source.imagem());
        Categoria categoria = Categoria.builder()
                .nome(source.nome().trim())
                .descricao(source.descricao())
                .build();

        if (imagem != null) {
            categoria.atualizarImagem(imagem);
        }

        return categoria;
    }
}
