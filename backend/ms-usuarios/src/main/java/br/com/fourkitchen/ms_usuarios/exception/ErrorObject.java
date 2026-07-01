package br.com.fourkitchen.ms_usuarios.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta padrao de erro da API.")
public class ErrorObject {

    @Schema(description = "Codigo interno do erro", example = "004")
    private String codError;

    @Schema(description = "Mensagem do erro", example = "Dados invalidos")
    private String msgError;
}
