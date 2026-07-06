package br.com.fourkitchen.bff_restaurante.client.usuarios;

import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.AtualizarUsuarioClientRequest;
import br.com.fourkitchen.bff_restaurante.client.usuarios.dto.UsuarioClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "ms-usuarios", url = "${clients.ms-usuarios.url}")
public interface UsuarioClient {

    @GetMapping("/api/usuarios")
    List<UsuarioClientResponse> listarUsuariosAtivos(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );

    @PutMapping("/api/usuarios/{id}")
    UsuarioClientResponse atualizarUsuario(
            @PathVariable Integer id,
            @RequestBody AtualizarUsuarioClientRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );

    @DeleteMapping("/api/usuarios/{id}")
    void inativarUsuario(
            @PathVariable Integer id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );
}
