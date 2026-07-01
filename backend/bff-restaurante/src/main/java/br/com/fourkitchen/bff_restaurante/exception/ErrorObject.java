package br.com.fourkitchen.bff_restaurante.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta padrao de erro da API.")
public class ErrorObject {

    @Schema(description = "Codigo interno do erro", example = "002")
    private String codError;

    @Schema(description = "Mensagem do erro", example = "Token invalido ou expirado")
    private String msgError;
}
