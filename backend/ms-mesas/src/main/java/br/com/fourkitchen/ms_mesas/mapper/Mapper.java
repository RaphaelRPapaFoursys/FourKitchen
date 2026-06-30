package br.com.fourkitchen.ms_mesas.mapper;

/**
 * Contrato base para classes responsaveis por converter um objeto de origem em
 * outro objeto de destino.
 *
 * <p>Use esta interface quando precisar isolar a regra de transformacao entre
 * camadas da aplicacao, por exemplo de um DTO de request para uma entidade, ou
 * de uma entidade para um DTO de response. As implementacoes devem ser
 * registradas como componentes do Spring com {@code @Component} quando forem
 * injetadas em services, controllers ou outros beans.</p>
 *
 * <p>Exemplo de implementacao:</p>
 *
 * <pre>{@code
 * @Component
 * public class MesaResponseMapper implements Mapper<Mesa, MesaResponse> {
 *     @Override
 *     public MesaResponse map(Mesa source) {
 *         return new MesaResponse(
 *                 source.getId(),
 *                 source.getNumero(),
 *                 Boolean.TRUE.equals(source.getDisponivel()) ? StatusMesa.DISPONIVEL : StatusMesa.OCUPADA,
 *                 source.getAtendimento() != null ? source.getAtendimento().getGarcomId() : null,
 *                 source.getAtendimento() != null ? source.getAtendimento().getCodigoSessao() : null,
 *                 source.getAtendimento() != null ? source.getAtendimento().getDataAbertura() : null,
 *                 source.getAtendimento() != null ? source.getAtendimento().getDataFechamento() : null
 *         );
 *     }
 * }
 * }</pre>
 *
 * <p>Exemplo de uso:</p>
 *
 * <pre>{@code
 * private final Mapper<Mesa, MesaResponse> mesaResponseMapper;
 *
 * public MesaResponse buscarMesa(Mesa mesa) {
 *     return mesaResponseMapper.map(mesa);
 * }
 * }</pre>
 *
 * @param <S> tipo do objeto de origem, como um request, entidade ou outro DTO
 * @param <T> tipo do objeto de destino retornado pelo mapper
 */
public interface Mapper<S, T> {

    /**
     * Converte o objeto de origem informado para o tipo de destino definido na
     * implementacao.
     *
     * @param source objeto que sera usado como base para a conversao
     * @return objeto convertido para o tipo de destino
     */
    T map(S source);
}
