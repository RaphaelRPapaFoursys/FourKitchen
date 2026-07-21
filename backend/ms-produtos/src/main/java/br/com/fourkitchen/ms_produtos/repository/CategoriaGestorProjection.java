package br.com.fourkitchen.ms_produtos.repository;

import java.time.Instant;

public interface CategoriaGestorProjection {

    Integer getId();

    String getNome();

    String getDescricao();

    Boolean getAtivo();

    Instant getImagemAtualizadaEm();

    Boolean getPossuiImagem();
}
