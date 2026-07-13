package br.com.fourkitchen.ms_produtos.repository;

import java.time.Instant;

public interface CategoriaCardapioProjection {

    Integer getId();

    String getNome();

    String getDescricao();

    Boolean getPossuiImagem();

    Instant getImagemAtualizadaEm();
}
