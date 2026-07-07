package br.com.fourkitchen.ms_usuarios.validation;

public final class UsuarioRegex {

    public static final String SENHA_FORTE = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

    private UsuarioRegex() {
    }
}
