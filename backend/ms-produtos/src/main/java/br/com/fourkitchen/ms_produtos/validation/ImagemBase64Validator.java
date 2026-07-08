package br.com.fourkitchen.ms_produtos.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador da anotacao {@link ImagemBase64}.
 *
 * <p>Normaliza o valor recebido removendo prefixo de Data URL, quando existir,
 * valida o Base64 e garante que o arquivo seja uma imagem JPG/JPEG ou PNG,
 * com ate 1 MB, dimensoes maximas de 1200x900 e proporcao 4:3.</p>
 */
public class ImagemBase64Validator implements ConstraintValidator<ImagemBase64, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            ImagemBase64Utils.decodificarEValidar(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
