package br.com.fourkitchen.ms_produtos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MsProdutosApplicationTests {

	@Test
	void applicationClassDeveExistir() {
		MsProdutosApplication application = new MsProdutosApplication();

		assertNotNull(application);
	}

}
