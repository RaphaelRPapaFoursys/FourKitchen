package br.com.fourkitchen.ms_produtos.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImagemBase64ValidatorTest {

    private final ImagemBase64Validator validator = new ImagemBase64Validator();

    @Test
    void isValidDeveAceitarImagemNulaOuVazia() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid(" ", null));
    }

    @Test
    void isValidDeveAceitarBase64Puro() {
        assertTrue(validator.isValid("aW1hZ2Vt", null));
    }

    @Test
    void isValidDeveAceitarDataUrlBase64() {
        assertTrue(validator.isValid("data:image/png;base64,aW1hZ2Vt", null));
    }

    @Test
    void isValidDeveBloquearBase64Invalido() {
        assertFalse(validator.isValid("base64-invalido", null));
    }
}
