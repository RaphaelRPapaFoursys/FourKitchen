package br.com.fourkitchen.bff_restaurante.mapper;

public interface Mapper<S, T> {

    T map(S source);
}
