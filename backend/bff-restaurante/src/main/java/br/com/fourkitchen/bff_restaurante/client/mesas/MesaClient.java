package br.com.fourkitchen.bff_restaurante.client.mesas;

import br.com.fourkitchen.bff_restaurante.client.mesas.dto.AtribuirGarcomClientRequest;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaClientResponse;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.MesaGarcomClientResponse;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.SessaoMesaResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "ms-mesas", url = "${clients.ms-mesas.url}")
public interface MesaClient {

    @GetMapping("/api/mesas")
    List<MesaClientResponse> listarMesas();

    @PatchMapping("/api/mesas/{id}/abrir")
    MesaClientResponse abrirMesa(@PathVariable Integer id);

    @PatchMapping("/api/mesas/{id}/fechar")
    MesaClientResponse fecharMesa(@PathVariable Integer id);

    @PatchMapping("/api/mesas/{id}/atribuir-garcom")
    MesaClientResponse atribuirGarcom(
            @PathVariable Integer id,
            @RequestBody AtribuirGarcomClientRequest request
    );

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

    @GetMapping("/api/mesas/garcons/{idGarcom}")
    List<MesaGarcomClientResponse> listarMesasPorGarcom(@PathVariable Integer idGarcom);
}
