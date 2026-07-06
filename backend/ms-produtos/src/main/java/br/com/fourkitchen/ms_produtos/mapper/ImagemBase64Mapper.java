package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.exception.BaseException;
import br.com.fourkitchen.ms_produtos.exception.ErrorEnum;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class ImagemBase64Mapper {

    private static final String DATA_URL_BASE64_MARKER = ";base64,";

    public byte[] paraBytes(String imagem) {
        if (imagem == null || imagem.isBlank()) {
            return null;
        }

        try {
            return Base64.getDecoder().decode(normalizar(imagem));
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

    private String normalizar(String imagem) {
        String imagemNormalizada = imagem.trim();

        if (imagemNormalizada.startsWith("data:")) {
            int base64Index = imagemNormalizada.indexOf(DATA_URL_BASE64_MARKER);

            if (base64Index < 0) {
                throw new BaseException(ErrorEnum.DADOS_INVALIDOS);
            }

            imagemNormalizada = imagemNormalizada.substring(base64Index + DATA_URL_BASE64_MARKER.length());
        }

        return imagemNormalizada.replaceAll("\\s+", "");
    }
}
