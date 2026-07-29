package br.com.fourkitchen.bff_restaurante.controller;

import br.com.fourkitchen.bff_restaurante.exception.BaseException;
import br.com.fourkitchen.bff_restaurante.exception.ErrorEnum;
import br.com.fourkitchen.bff_restaurante.realtime.CanalRealtime;
import br.com.fourkitchen.bff_restaurante.realtime.SseEmitterRegistry;
import br.com.fourkitchen.bff_restaurante.security.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/realtime")
public class RealtimeController {

    private final SseEmitterRegistry emitterRegistry;

    @GetMapping(path = "/eventos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter assinar(Authentication authentication, HttpServletResponse response) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new BaseException(ErrorEnum.TOKEN_INVALIDO);
        }

        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");

        return emitterRegistry.registrar(CanalRealtime.doUsuario(usuario));
    }
}
