package br.com.fourkitchen.ms_pagamentos.controller;

import br.com.fourkitchen.ms_pagamentos.dto.response.PagamentoResponse;
import br.com.fourkitchen.ms_pagamentos.service.PagamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {
    private final PagamentoService service;

    @PostMapping
    public ResponseEntity<PagamentoResponse> pagar(){
        return ResponseEntity.ok(service.pagar());
    }
}
