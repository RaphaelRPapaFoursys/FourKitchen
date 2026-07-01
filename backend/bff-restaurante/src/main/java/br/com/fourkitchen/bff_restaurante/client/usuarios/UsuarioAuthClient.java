package br.com.fourkitchen.bff_restaurante.client.usuarios;

import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioLoginRequest;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioLoginResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-usuarios-auth", url = "${clients.ms-usuarios.url}")
public interface UsuarioAuthClient {

    @PostMapping("/auth/login")
    UsuarioLoginResponse login(@RequestBody UsuarioLoginRequest request);
}
