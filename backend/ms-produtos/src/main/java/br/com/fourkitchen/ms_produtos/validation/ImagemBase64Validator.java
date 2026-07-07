package br.com.fourkitchen.ms_produtos.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Base64;

/**
 * Validador da anotacao {@link ImagemBase64}.
 *
 * <p>Normaliza o valor recebido removendo prefixo de Data URL, quando existir,
 * e espacos em branco antes de validar com o decoder Base64.</p>
 */
public class ImagemBase64Validator implements ConstraintValidator<ImagemBase64, String> {

    private static final String DATA_URL_BASE64_MARKER = ";base64,";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            Base64.getDecoder().decode(normalizar(value));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String normalizar(String imagem) {
        String imagemNormalizada = imagem.trim();

        if (imagemNormalizada.startsWith("data:")) {
            int base64Index = imagemNormalizada.indexOf(DATA_URL_BASE64_MARKER);

            if (base64Index < 0) {
                throw new IllegalArgumentException("Data URL sem conteudo Base64.");
            }

            imagemNormalizada = imagemNormalizada.substring(base64Index + DATA_URL_BASE64_MARKER.length());
        }

        return imagemNormalizada.replaceAll("\\s+", "");
    }
}
