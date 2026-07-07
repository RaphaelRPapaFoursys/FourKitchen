package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.request.ChamarGarcomRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.NotificacaoResponse;
import br.com.fourkitchen.bff_restaurante.exception.ErrorObject;
import br.com.fourkitchen.bff_restaurante.service.MesaChamadaGarcomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mesa/chamadas-garcom")
@Tag(name = "Chamadas da Mesa", description = "Rotas usadas pelo tablet da mesa para chamar o garcom responsavel.")
public class MesaChamadaGarcomController {

    private final MesaChamadaGarcomService mesaChamadaGarcomService;

    @PostMapping
    @Operation(
            summary = "Chama o garcom responsavel pela mesa",
            description = "Valida a sessao da mesa ocupada, identifica o garcom responsavel pelo atendimento e cria uma notificacao pendente CHAMADA_GARCOM somente para esse garcom."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Chamada criada com sucesso",
                    content = @Content(
                            schema = @Schema(implementation = NotificacaoResponse.class),
                            examples = @ExampleObject(value = "{\"id\":3,\"tipo\":\"CHAMADA_GARCOM\",\"mensagem\":\"Cliente solicitou atendimento\",\"destino\":\"GARCOM\",\"lida\":false,\"data\":\"2026-07-02T10:15:30\",\"idMesa\":1,\"idAtendimento\":8,\"idGarcom\":7}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Sessao da mesa invalida, mesa sem garcom ou dados invalidos",
                    content = @Content(
                            schema = @Schema(implementation = ErrorObject.class),
                            examples = {
                                    @ExampleObject(name = "Sessao invalida", value = "{\"codError\":\"006\",\"msgError\":\"Sessao da mesa invalida\"}"),
                                    @ExampleObject(name = "Mesa sem garcom", value = "{\"codError\":\"016\",\"msgError\":\"Mesa sem garcom responsavel\"}")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Servico de mesas ou notificacoes indisponivel",
                    content = @Content(schema = @Schema(implementation = ErrorObject.class))
            )
    })
    public ResponseEntity<NotificacaoResponse> chamarGarcom(
            @RequestBody @Valid ChamarGarcomRequest request
    ) {
        NotificacaoResponse response = mesaChamadaGarcomService.chamarGarcom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
