package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.CategoriaResponse;
import br.com.fourkitchen.ms_produtos.model.Categoria;
import br.com.fourkitchen.ms_produtos.repository.CategoriaGestorProjection;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CategoriaResponseMapper implements Mapper<Categoria, CategoriaResponse> {

    @Override
    public CategoriaResponse map(Categoria source) {
        return new CategoriaResponse(
                source.getId(),
                source.getNome(),
                source.getDescricao(),
                criarImagemUrl(source.getId(), source.getImagem(), source.getImagemAtualizadaEm()),
                source.getAtivo()
        );
    }

    public CategoriaResponse map(CategoriaGestorProjection source) {
        return new CategoriaResponse(
                source.getId(),
                source.getNome(),
                source.getDescricao(),
                criarImagemUrl(source.getId(), source.getPossuiImagem(), source.getImagemAtualizadaEm()),
                source.getAtivo()
        );
    }

    private String criarImagemUrl(Integer id, byte[] imagem, Instant atualizadaEm) {
        return criarImagemUrl(id, imagem != null && imagem.length > 0, atualizadaEm);
    }

    private String criarImagemUrl(Integer id, Boolean possuiImagem, Instant atualizadaEm) {
        if (!Boolean.TRUE.equals(possuiImagem)) {
            return null;
        }

        long versao = atualizadaEm == null ? 0 : atualizadaEm.toEpochMilli();
        return "/api/categorias/" + id + "/imagem?v=" + versao;
    }
}
