package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.client.pagamentos.dto.PagamentoResponse;
import br.com.fourkitchen.bff_restaurante.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor

@Tag(name = "Pagamentos", description = "Endpoints responsáveis pelo processamento de pagamentos.")
public class PagamentoController {
    private final PagamentoService service;

    @Operation(
            summary = "Processar pagamento",
            description = """
                    Recebe uma solicitação de pagamento e a encaminha ao microsserviço de pagamentos.
                    O resultado da operação (aprovação ou recusa) é retornado ao cliente.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pagamento processado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "402",
                    description = "Pagamento recusado."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno ao processar o pagamento."
            )
    })
    @PostMapping
    public ResponseEntity<PagamentoResponse> pagar() {
        return ResponseEntity.ok(service.pagar());
    }

}
