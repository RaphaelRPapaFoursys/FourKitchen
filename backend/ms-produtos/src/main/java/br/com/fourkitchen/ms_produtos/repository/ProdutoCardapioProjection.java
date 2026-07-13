package br.com.fourkitchen.ms_produtos.repository;

import java.math.BigDecimal;

public interface ProdutoCardapioProjection {

    Integer getId();

    String getNome();

    String getDescricao();

    BigDecimal getPreco();

    Integer getCategoriaId();

    String getCategoriaNome();

    String getCategoriaDescricao();

    Boolean getPossuiImagem();
}
