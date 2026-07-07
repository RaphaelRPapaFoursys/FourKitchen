package br.com.fourkitchen.ms_pedidos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MsPedidosApplicationTests {

    @Test
    void applicationClassDeveExistir() {
        MsPedidosApplication application = new MsPedidosApplication();

        assertNotNull(application);
    }

}
