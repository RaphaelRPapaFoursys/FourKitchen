package br.com.fourkitchen.ms_produtos.validation;

import org.junit.jupiter.api.Test;

import static br.com.fourkitchen.ms_produtos.support.ImagemTesteFactory.criarBase64FormatoNaoPermitido;
import static br.com.fourkitchen.ms_produtos.support.ImagemTesteFactory.criarDataUrlPng;
import static br.com.fourkitchen.ms_produtos.support.ImagemTesteFactory.criarJpegBase64;
import static br.com.fourkitchen.ms_produtos.support.ImagemTesteFactory.criarPngAcimaDoTamanhoMaximoBase64;
import static br.com.fourkitchen.ms_produtos.support.ImagemTesteFactory.criarPngBase64;
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
    void isValidDeveAceitarPngEmBase64Puro() {
        assertTrue(validator.isValid(criarPngBase64(800, 600), null));
    }

    @Test
    void isValidDeveAceitarJpegEmBase64Puro() {
        assertTrue(validator.isValid(criarJpegBase64(800, 600), null));
    }

    @Test
    void isValidDeveAceitarDataUrlBase64() {
        assertTrue(validator.isValid(criarDataUrlPng(800, 600), null));
    }

    @Test
    void isValidDeveBloquearBase64Invalido() {
        assertFalse(validator.isValid("base64-invalido", null));
    }

    @Test
    void isValidDeveBloquearFormatoNaoPermitido() {
        assertFalse(validator.isValid(criarBase64FormatoNaoPermitido(), null));
    }

    @Test
    void isValidDeveBloquearImagemAcimaDoTamanhoMaximo() {
        assertFalse(validator.isValid(criarPngAcimaDoTamanhoMaximoBase64(), null));
    }

    @Test
    void isValidDeveBloquearImagemAcimaDasDimensoesMaximas() {
        assertFalse(validator.isValid(criarPngBase64(1600, 1200), null));
    }

    @Test
    void isValidDeveBloquearImagemForaDaProporcaoQuatroPorTres() {
        assertFalse(validator.isValid(criarPngBase64(800, 500), null));
    }
}
