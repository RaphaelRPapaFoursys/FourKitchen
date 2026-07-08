package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.exception.BaseException;
import br.com.fourkitchen.ms_produtos.exception.ErrorEnum;
import br.com.fourkitchen.ms_produtos.validation.ImagemBase64Utils;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class ImagemBase64Mapper {

    public byte[] paraBytes(String imagem) {
        if (imagem == null || imagem.isBlank()) {
            return null;
        }

        try {
            return ImagemBase64Utils.decodificarEValidar(imagem);
        } catch (IllegalArgumentException e) {
            throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
        }
    }

    public String paraBase64(byte[] imagem) {
        if (imagem == null || imagem.length == 0) {
            return null;
        }

        return Base64.getEncoder().encodeToString(imagem);
    }
}
