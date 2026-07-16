package br.com.fourkitchen.bff_restaurante.realtime;

public final class RealtimeDestination {

    public static final String COZINHA_PEDIDOS = "/topic/cozinha/pedidos";
    public static final String GARCOM_OPERACAO = "/topic/garcom/operacao";
    public static final String GESTOR_OPERACAO = "/topic/gestor/operacao";
    public static final String GESTOR_CATALOGO = "/topic/gestor/catalogo";
    public static final String GESTOR_USUARIOS = "/topic/gestor/usuarios";
    public static final String CARDAPIO = "/topic/cardapio";

    private RealtimeDestination() {
    }

    public static String mesa(Integer idMesa) {
        return "/topic/mesas/" + idMesa;
    }
}
