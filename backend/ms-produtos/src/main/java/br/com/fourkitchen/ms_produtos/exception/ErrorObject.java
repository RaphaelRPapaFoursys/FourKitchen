package br.com.fourkitchen.ms_produtos.exception;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorObject {

    private String codError;
    private String msgError;
}
