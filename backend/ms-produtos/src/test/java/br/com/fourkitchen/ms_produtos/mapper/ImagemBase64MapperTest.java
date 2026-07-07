package br.com.fourkitchen.ms_produtos.mapper;

import br.com.fourkitchen.ms_produtos.exception.BaseException;
import br.com.fourkitchen.ms_produtos.exception.ErrorEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImagemBase64MapperTest {

    private final ImagemBase64Mapper imagemBase64Mapper = new ImagemBase64Mapper();

    @Test
    void paraBytesDeveConverterBase64Puro() {
        assertArrayEquals("imagem".getBytes(), imagemBase64Mapper.paraBytes("aW1hZ2Vt"));
    }

    @Test
    void paraBytesDeveConverterDataUrlBase64() {
        assertArrayEquals(
                "imagem".getBytes(),
                imagemBase64Mapper.paraBytes("data:image/png;base64,aW1hZ2Vt")
        );
    }

    @Test
    void paraBytesDeveRetornarNuloQuandoImagemForNulaOuVazia() {
        assertNull(imagemBase64Mapper.paraBytes(null));
        assertNull(imagemBase64Mapper.paraBytes(" "));
    }

    @Test
    void paraBytesDeveLancarDadosInvalidosQuandoBase64ForInvalido() {
        BaseException exception = assertThrows(
                BaseException.class,
                () -> imagemBase64Mapper.paraBytes("base64-invalido")
        );

        assertEquals(ErrorEnum.DADOS_INVALIDOS, exception.getErrorEnum());
    }

    @Test
    void paraBase64DeveConverterBytesParaBase64() {
        assertEquals("aW1hZ2Vt", imagemBase64Mapper.paraBase64("imagem".getBytes()));
    }
}
