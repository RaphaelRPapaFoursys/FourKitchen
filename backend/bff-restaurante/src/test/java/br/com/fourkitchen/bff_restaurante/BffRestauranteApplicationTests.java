package br.com.fourkitchen.bff_restaurante;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = "jwt.secret=chave-super-secreta-para-testes-fourkitchen-123456789")
@Import(BffRestauranteApplicationTests.ProtectedRoutesTestController.class)
class BffRestauranteApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void openApiDocsDeveDocumentarAutenticacaoDoBff() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("/api/auth/login")))
				.andExpect(content().string(containsString("/api/auth/me")))
				.andExpect(content().string(containsString("/api/notificacoes/pendentes")))
				.andExpect(content().string(containsString("/api/mesa/pedidos")))
				.andExpect(content().string(containsString("/api/totem/pedidos")))
				.andExpect(content().string(containsString("/api/garcom/chamadas/{id}/concluir")))
				.andExpect(content().string(containsString("/api/gestor/resumo")))
				.andExpect(content().string(containsString("O preco nao deve ser enviado pelo front")))
				.andExpect(content().string(containsString("bearerAuth")));
	}

	@Test
	void rotaDeGarcomDeveExigirAutenticacao() throws Exception {
		mockMvc.perform(get("/api/garcom/painel"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "COZINHA")
	void rotaDeGarcomDeveBloquearUsuarioSemPerfilPermitido() throws Exception {
		mockMvc.perform(get("/api/garcom/painel"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "GARCOM")
	void rotaDeGarcomDevePermitirPerfilGarcom() throws Exception {
		mockMvc.perform(get("/api/garcom/painel"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void rotaDeGarcomDeveBloquearPerfilAdmin() throws Exception {
		mockMvc.perform(get("/api/garcom/painel"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "GARCOM")
	void rotaDeCozinhaDeveBloquearUsuarioSemPerfilPermitido() throws Exception {
		mockMvc.perform(get("/api/cozinha/painel"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "COZINHA")
	void rotaDeCozinhaDevePermitirPerfilCozinha() throws Exception {
		mockMvc.perform(get("/api/cozinha/painel"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "COZINHA")
	void rotaDeGestorDeveBloquearUsuarioSemPerfilPermitido() throws Exception {
		mockMvc.perform(get("/api/gestor/painel"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void rotaDeGestorDevePermitirPerfilAdmin() throws Exception {
		mockMvc.perform(get("/api/gestor/painel"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "GESTOR")
	void rotaDeGestorDevePermitirPerfilGestor() throws Exception {
		mockMvc.perform(get("/api/gestor/painel"))
				.andExpect(status().isOk());
	}

	@RestController
	static class ProtectedRoutesTestController {

		@GetMapping("/api/garcom/painel")
		String garcom() {
			return "garcom";
		}

		@GetMapping("/api/cozinha/painel")
		String cozinha() {
			return "cozinha";
		}

		@GetMapping("/api/gestor/painel")
		String gestor() {
			return "gestor";
		}
	}

}
