package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.request.AtualizarCategoriaRequest;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import org.springframework.stereotype.Component;

@Component
public class AtualizarCategoriaRequestMapper {

    private final ImagemBase64Mapper imagemBase64Mapper;

    public AtualizarCategoriaRequestMapper(ImagemBase64Mapper imagemBase64Mapper) {
        this.imagemBase64Mapper = imagemBase64Mapper;
    }

    public void map(AtualizarCategoriaRequest source, Categoria target) {
        target.setNome(source.nome().trim());
        target.setDescricao(source.descricao());
        if (source.imagem() != null) {
            target.setImagem(imagemBase64Mapper.paraBytes(source.imagem()));
        }
    }
}
