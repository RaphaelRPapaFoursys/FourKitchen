package br.com.fourkitchen.ms_pedidos.exceptions;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorObject {
    private String codError;
    private String msgError;
}