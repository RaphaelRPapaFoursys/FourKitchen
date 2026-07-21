package br.com.fourkitchen.ms_produtos.repository;

import java.math.BigDecimal;
import java.time.Instant;

public interface ProdutoGestorProjection {

    Integer getId();

    String getNome();

    String getDescricao();

    BigDecimal getPreco();

    Integer getCategoriaId();

    String getCategoria();

    Boolean getDisponivel();

    Instant getImagemAtualizadaEm();

    Boolean getPossuiImagem();
}
