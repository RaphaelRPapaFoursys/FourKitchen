package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.AssumirProblemaTotemRequest;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.DecisaoProblemaRequest;
import br.com.fourkitchen.bff_restaurante.dto.response.ItemPedidoDetalheGarcomResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.PedidoProblemaTotemGarcomResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GarcomTotemProblemaService {

    private final PedidoClient pedidoClient;
    private final DecisaoProblemaService decisaoProblemaService;

    public List<PedidoProblemaTotemGarcomResponse> listarProblemas(Authentication authentication) {
        extrairIdGarcom(authentication);
        try {
            return pedidoClient.listarProblemasTotem().stream()
                    .map(pedido -> new PedidoProblemaTotemGarcomResponse(
                            pedido.id(),
                            pedido.codigo(),
                            pedido.status(),
                            pedido.dataCriacao(),
                            pedido.idGarcomResponsavelProblema(),
                            (pedido.itens() == null ? List.<ItemPedidoCozinhaResponse>of() : pedido.itens()).stream()
                                    .map(item -> new ItemPedidoDetalheGarcomResponse(
                                            item.id(), item.idProduto(), item.nomeProduto(), item.quantidade(),
                                            item.precoUnitario(), item.observacao(), item.status()
                                    ))
                                    .toList()
                    ))
                    .toList();
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    public void assumir(Integer idPedido, Authentication authentication) {
        Integer idGarcom = extrairIdGarcom(authentication);
        if (idPedido == null || idPedido <= 0) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }

        try {
            pedidoClient.assumirProblemaTotem(idPedido, new AssumirProblemaTotemRequest(idGarcom));
        } catch (FeignException e) {
            tratarErroPedido(e);
        }
    }

    public void registrarDecisao(Integer idPedido, DecisaoProblemaRequest request, Authentication authentication) {
        Integer idGarcom = extrairIdGarcom(authentication);
        if (idPedido == null || idPedido <= 0 || request == null || !idPedido.equals(request.idPedido())) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
        decisaoProblemaService.registrarProblemaTotem(request, idGarcom);
    }

    private Integer extrairIdGarcom(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)
                || usuario.id() == null) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }
        try {
            return Math.toIntExact(usuario.id());
        } catch (ArithmeticException e) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
    }

    private void tratarErroPedido(FeignException e) {
        if (e.status() == 400) {
            throw new BaseException(ErrorEnum.PEDIDO_NAO_PERMITE_DECISAO);
        }
        if (e.status() == 404) {
            throw new BaseException(ErrorEnum.PEDIDO_NAO_ENCONTRADO);
        }
        if (e.status() == 409) {
            throw new BaseException(ErrorEnum.PROBLEMA_TOTEM_NAO_DISPONIVEL);
        }
        throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
    }
}
