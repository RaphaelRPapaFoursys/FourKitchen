package br.com.fourkitchen.bff_restaurante.realtime;

import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CanalRealtimeTest {

    @Test
    void deveResolverCanaisDosPerfisGlobais() {
        assertEquals(CanalRealtime.COZINHA, CanalRealtime.doUsuario(usuario(1L, "cozinha", null)));
        assertEquals(CanalRealtime.GESTOR, CanalRealtime.doUsuario(usuario(1L, "GESTOR", null)));
        assertEquals(CanalRealtime.BALCAO, CanalRealtime.doUsuario(usuario(1L, "BALCAO", null)));
        assertEquals(CanalRealtime.ADMIN, CanalRealtime.doUsuario(usuario(1L, "ADMIN", null)));
    }

    @Test
    void deveResolverCanaisRestritosPorIdentificador() {
        assertEquals("MESA:3", CanalRealtime.doUsuario(usuario(1L, "MESA", 3)));
        assertEquals("GARCOM:7", CanalRealtime.doUsuario(usuario(7L, "GARCOM", null)));
        assertEquals("TOTEM:9", CanalRealtime.doUsuario(usuario(9L, "TOTEM", null)));
        assertEquals("MESA:3", CanalRealtime.mesa(3));
        assertEquals("GARCOM:7", CanalRealtime.garcom(7));
        assertEquals("TOTEM:9", CanalRealtime.totem(9));
    }

    @Test
    void deveRejeitarUsuarioOuPerfilInvalido() {
        assertErro(ErrorEnum.TOKEN_INVALIDO, () -> CanalRealtime.doUsuario(null));
        assertErro(ErrorEnum.TOKEN_INVALIDO, () -> CanalRealtime.doUsuario(usuario(1L, null, null)));
        assertErro(ErrorEnum.ACESSO_NEGADO, () -> CanalRealtime.doUsuario(usuario(1L, "CLIENTE", null)));
    }

    @Test
    void deveRejeitarIdentificadorInvalido() {
        assertErro(ErrorEnum.ACESSO_NEGADO, () -> CanalRealtime.mesa(null));
        assertErro(ErrorEnum.ACESSO_NEGADO, () -> CanalRealtime.garcom(0));
        assertErro(ErrorEnum.ACESSO_NEGADO, () ->
                CanalRealtime.doUsuario(usuario((long) Integer.MAX_VALUE + 1, "TOTEM", null)));
    }

    private UsuarioAutenticado usuario(Long id, String perfil, Integer idMesa) {
        return new UsuarioAutenticado(id, "Usuario", "usuario@teste.com", perfil, idMesa);
    }

    private void assertErro(ErrorEnum esperado, Runnable acao) {
        BaseException exception = assertThrows(BaseException.class, acao::run);
        assertEquals(esperado, exception.getErrorEnum());
    }
}
