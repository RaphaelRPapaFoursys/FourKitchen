package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.dto.response.TotemGestorResponse;
import br.com.fourkitchen.bff_restaurante.service.GestorTotemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorTotemControllerTest {

    @Mock
    private GestorTotemService service;

    @InjectMocks
    private GestorTotemController controller;

    @Test
    void listarTotensDeveRetornarOk() {
        TotemGestorResponse totem = new TotemGestorResponse(9, "Totem 01", "totem01@fourkitchen.com", true, 0L, BigDecimal.ZERO, null, 0L);
        when(service.listarTotens("Bearer token")).thenReturn(List.of(totem));

        ResponseEntity<List<TotemGestorResponse>> response = controller.listarTotens("Bearer token");

        assertEquals(200, response.getStatusCode().value());
        assertSame(totem, response.getBody().getFirst());
        verify(service).listarTotens("Bearer token");
    }
}
