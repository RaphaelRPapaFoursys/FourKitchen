package br.com.fourkitchen.ms_notificacoes.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorObject {

    private String codError;

    private String msgError;
}
