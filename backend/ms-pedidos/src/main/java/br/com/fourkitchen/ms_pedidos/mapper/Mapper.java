package br.com.fourkitchen.ms_pedidos.mapper;

public interface Mapper<S, T> {
    T map(S source);
}