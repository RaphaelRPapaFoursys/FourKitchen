package br.com.fourkitchen.bff_restaurante.client.mesas;

import br.com.fourkitchen.bff_restaurante.client.mesas.dto.SessaoMesaResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-mesas", url = "${clients.ms-mesas.url}")
public interface MesaClient {

    @GetMapping("/api/mesas/{idMesa}/sessoes/{codigoSessao}/validar")
    SessaoMesaResponse validarSessaoMesa(
            @PathVariable Integer idMesa,
            @PathVariable Integer codigoSessao
    );

    @GetMapping("/api/mesas/{idMesa}/garcons/{idGarcom}/validar")
    SessaoMesaResponse validarMesaAtribuidaGarcom(
            @PathVariable Integer idMesa,
            @PathVariable Integer idGarcom
    );
}
