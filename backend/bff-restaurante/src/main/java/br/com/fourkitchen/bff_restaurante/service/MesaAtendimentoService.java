package br.com.fourkitchen.bff_restaurante.service;

import br.com.fourkitchen.bff_restaurante.client.mesas.MesaClient;
import br.com.fourkitchen.bff_restaurante.client.mesas.dto.SessaoMesaResponse;
import br.com.fourkitchen.bff_restaurante.dto.response.MesaAtendimentoAtualResponse;
import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MesaAtendimentoService {

    private static final String PERFIL_MESA = "MESA";

    private final MesaClient mesaClient;

    public MesaAtendimentoAtualResponse buscarAtendimentoAtual(Authentication authentication) {
        UsuarioAutenticado usuario = obterUsuarioMesa(authentication);

        try {
            SessaoMesaResponse sessao = mesaClient.buscarAtendimentoAtual(usuario.idMesa());

            return new MesaAtendimentoAtualResponse(
                    sessao.idMesa(),
                    sessao.idAtendimento(),
                    sessao.codigoSessao(),
                    sessao.status()
            );
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new BaseException(ErrorEnum.ATENDIMENTO_NAO_ABERTO);
            }

            if (e.status() == 404) {
                throw new BaseException(ErrorEnum.MESA_NAO_ENCONTRADA);
            }

            throw new BaseException(ErrorEnum.MS_MESAS_INDISPONIVEL);
        }
    }

    private UsuarioAutenticado obterUsuarioMesa(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        if (!PERFIL_MESA.equals(usuario.perfil()) || usuario.idMesa() == null || usuario.idMesa() <= 0) {
            throw new BaseException(ErrorEnum.ACESSO_NEGADO);
        }

        return usuario;
    }
}
