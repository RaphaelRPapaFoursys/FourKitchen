package br.com.fourkitchen.bff_restaurante.client.usuarios;

import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "ms-usuarios", url = "${clients.ms-usuarios.url}")
public interface UsuarioClient {

    @GetMapping("/api/usuarios")
    List<UsuarioClientResponse> listarUsuariosAtivos(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );
}
