package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.SessaoMesaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.PedidoClient;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.ItemPedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.client.pedidos.dto.PedidoCozinhaResponse;
import br.com.fourkitchen.bff_restaurante.dto.request.DecisaoProblemaRequest;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GarcomProblemaService {

    private static final List<String> STATUS_PERMITE_DECISAO = List.of(
            "AGUARDANDO_DECISAO",
            "PROBLEMA_COZINHA"
    );

    private static final List<String> STATUS_ITEM_COM_PROBLEMA = List.of(
            "FALTA_PRODUTO",
            "ERRO",
            "INDISPONIVEL"
    );

    private final MesaClient mesaClient;

    private final PedidoClient pedidoClient;

    private final DecisaoProblemaService decisaoProblemaService;

    public void registrarDecisao(
            Integer idMesa,
            DecisaoProblemaRequest request,
            Authentication authentication
    ) {
        Integer idGarcom = extrairIdGarcom(authentication);
        SessaoMesaResponse sessao = validarMesaDoGarcom(idMesa, idGarcom);
        PedidoCozinhaResponse pedido = buscarPedidoDoAtendimento(sessao, idMesa, request.idPedido());

        validarProblemaDoItem(pedido, request.idProdutoPedido());
        decisaoProblemaService.registrar(request);
    }

    private Integer extrairIdGarcom(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        if (usuario.id() == null) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        try {
            return Math.toIntExact(usuario.id());
        } catch (ArithmeticException e) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
    }

    private SessaoMesaResponse validarMesaDoGarcom(Integer idMesa, Integer idGarcom) {
        if (idMesa == null || idMesa <= 0) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }

        try {
            return mesaClient.validarMesaAtribuidaGarcom(idMesa, idGarcom);
        } catch (FeignException e) {
            if (e.status() == 403) {
                throw new BaseException(ErrorEnum.MESA_NAO_ATRIBUIDA_AO_GARCOM);
            }

            if (e.status() == 400 || e.status() == 404) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private PedidoCozinhaResponse buscarPedidoDoAtendimento(
            SessaoMesaResponse sessao,
            Integer idMesa,
            Integer idPedido
    ) {
        if (idPedido == null || sessao.idAtendimento() == null) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }

        try {
            return pedidoClient.listarPedidosDetalhadosPorAtendimento(sessao.idAtendimento())
                    .stream()
                    .filter(pedido -> Objects.equals(pedido.id(), idPedido))
                    .filter(pedido -> Objects.equals(pedido.idMesa(), idMesa))
                    .filter(pedido -> Objects.equals(pedido.idAtendimento(), sessao.idAtendimento()))
                    .filter(pedido -> STATUS_PERMITE_DECISAO.contains(pedido.status()))
                    .findFirst()
                    .orElseThrow(() -> new BaseException(ErrorEnum.PEDIDO_NAO_PERMITE_DECISAO));
        } catch (FeignException e) {
            throw new BaseException(ErrorEnum.MS_PEDIDOS_INDISPONIVEL);
        }
    }

    private void validarProblemaDoItem(PedidoCozinhaResponse pedido, Integer idProdutoPedido) {
        if (idProdutoPedido == null) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }

        List<ItemPedidoCozinhaResponse> itens = pedido.itens() == null ? List.of() : pedido.itens();
        boolean itemComProblema = itens.stream()
                .anyMatch(item -> Objects.equals(item.id(), idProdutoPedido)
                        && STATUS_ITEM_COM_PROBLEMA.contains(item.status()));

        if (!itemComProblema) {
            throw new BaseException(ErrorEnum.PEDIDO_NAO_PERMITE_DECISAO);
        }
    }
}
