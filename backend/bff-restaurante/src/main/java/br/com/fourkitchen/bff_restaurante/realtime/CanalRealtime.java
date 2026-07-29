package br.com.fourkitchen.bff_restaurante.realtime;

import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;

import java.util.Locale;

public final class CanalRealtime {

    public static final String COZINHA = "COZINHA";
    public static final String GESTOR = "GESTOR";
    public static final String BALCAO = "BALCAO";
    public static final String ADMIN = "ADMIN";

    private CanalRealtime() {
    }

    public static String doUsuario(UsuarioAutenticado usuario) {
        if (usuario == null || usuario.perfil() == null) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        return switch (usuario.perfil().toUpperCase(Locale.ROOT)) {
            case "COZINHA" -> COZINHA;
            case "GESTOR" -> GESTOR;
            case "BALCAO" -> BALCAO;
            case "ADMIN" -> ADMIN;
            case "MESA" -> mesa(idObrigatorio(usuario.idMesa()));
            case "GARCOM" -> garcom(idObrigatorio(usuario.id()));
            case "TOTEM" -> totem(idObrigatorio(usuario.id()));
            default -> throw new BaseException(ErrorEnum.ACESSO_NEGADO);
        };
    }

    public static String mesa(Integer idMesa) {
        return "MESA:" + idObrigatorio(idMesa);
    }

    public static String garcom(Integer idGarcom) {
        return "GARCOM:" + idObrigatorio(idGarcom);
    }

    public static String totem(Integer idTotem) {
        return "TOTEM:" + idObrigatorio(idTotem);
    }

    private static int idObrigatorio(Number id) {
        if (id == null || id.longValue() <= 0 || id.longValue() > Integer.MAX_VALUE) {
            throw new BaseException(ErrorEnum.ACESSO_NEGADO);
        }

        return id.intValue();
    }
}
