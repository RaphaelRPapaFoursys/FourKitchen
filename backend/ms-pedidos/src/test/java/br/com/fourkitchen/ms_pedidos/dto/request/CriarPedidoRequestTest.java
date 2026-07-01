package br.com.fourkitchen.ms_pedidos.dto.request;

import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CriarPedidoRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void pedidoTotemSemMesaDeveSerValido() {
        CriarPedidoRequest request = criarRequest(CanaisPedido.TOTEM, null);

        Set<ConstraintViolation<CriarPedidoRequest>> violations = validator.validate(request);

        assertEquals(0, violations.size());
    }

    @Test
    void pedidoTotemComMesaDeveSerInvalido() {
        CriarPedidoRequest request = criarRequest(CanaisPedido.TOTEM, 1);

        Set<ConstraintViolation<CriarPedidoRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("Mesa invalida para o canal do pedido", violations.iterator().next().getMessage());
    }

    @Test
    void pedidoMesaSemMesaDeveSerInvalido() {
        CriarPedidoRequest request = criarRequest(CanaisPedido.MESA, null);

        Set<ConstraintViolation<CriarPedidoRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("Mesa invalida para o canal do pedido", violations.iterator().next().getMessage());
    }

    private CriarPedidoRequest criarRequest(CanaisPedido canal, Integer idMesa) {
        return new CriarPedidoRequest(
                null,
                null,
                canal,
                StatusPedido.ENVIADO_COZINHA,
                idMesa,
                null,
                null,
                null
        );
    }
}
