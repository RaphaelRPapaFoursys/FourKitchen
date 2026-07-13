package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.dto.response.ProdutoCardapioResponse;
import br.com.fourkitchen.ms_produtos.repository.ProdutoCardapioProjection;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ProdutoCardapioResponseMapper implements Mapper<ProdutoCardapioProjection, ProdutoCardapioResponse> {

    @Override
    public ProdutoCardapioResponse map(ProdutoCardapioProjection source) {
        return new ProdutoCardapioResponse(
                source.getId(),
                source.getNome(),
                source.getDescricao(),
                criarImagemUrl(source),
                source.getPreco()
        );
    }

    private String criarImagemUrl(ProdutoCardapioProjection source) {
        if (!Boolean.TRUE.equals(source.getPossuiImagem())) {
            return null;
        }

        Instant imagemAtualizadaEm = source.getImagemAtualizadaEm();
        long versao = imagemAtualizadaEm == null ? 0 : imagemAtualizadaEm.toEpochMilli();

        return "/api/produtos/" + source.getId() + "/imagem?v=" + versao;
    }
}
