package br.com.fourkitchen.ms_usuarios.mapper;

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
 * public class UsuarioResponseMapper implements Mapper<Usuario, UsuarioResponse> {
 *     @Override
 *     public UsuarioResponse map(Usuario source) {
 *         return new UsuarioResponse(
 *                 source.getId(),
 *                 source.getNome(),
 *                 source.getEmail(),
 *                 source.getPerfilUsuario(),
 *                 source.getAtivo()
 *         );
 *     }
 * }
 * }</pre>
 *
 * <p>Exemplo de uso:</p>
 *
 * <pre>{@code
 * private final Mapper<Usuario, UsuarioResponse> usuarioResponseMapper;
 *
 * public UsuarioResponse buscarUsuario(Usuario usuario) {
 *     return usuarioResponseMapper.map(usuario);
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
