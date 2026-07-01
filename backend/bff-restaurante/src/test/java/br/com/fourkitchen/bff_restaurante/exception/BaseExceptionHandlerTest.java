package br.com.fourkitchen.bff_restaurante.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BaseExceptionHandlerTest {

    private final BaseExceptionHandler baseExceptionHandler = new BaseExceptionHandler();

    @Test
    void handleBaseExceptionDeveRetornarErroDeNegocio() {
        ResponseEntity<ErrorObject> response = baseExceptionHandler.handleBaseException(
                new BaseException(ErrorEnum.TOKEN_INVALIDO)
        );

        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ErrorEnum.TOKEN_INVALIDO.getErrorCode(), response.getBody().getCodError());
        assertEquals(ErrorEnum.TOKEN_INVALIDO.getErrorMessage(), response.getBody().getMsgError());
    }

    @Test
    void handleMethodArgumentNotValidExceptionDeveRetornarPrimeiroErroDeValidacao() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "loginRequest");
        bindingResult.addError(new FieldError("loginRequest", "email", "Email invalido"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                methodParameter(),
                bindingResult
        );

        ResponseEntity<ErrorObject> response = baseExceptionHandler.handleMethodArgumentNotValidException(exception);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ErrorEnum.DADOS_INVALIDOS.getErrorCode(), response.getBody().getCodError());
        assertEquals("Email invalido", response.getBody().getMsgError());
    }

    @Test
    void handleMethodArgumentNotValidExceptionDeveRetornarMensagemPadraoQuandoNaoHouverFieldError() throws Exception {
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                methodParameter(),
                new BeanPropertyBindingResult(new Object(), "loginRequest")
        );

        ResponseEntity<ErrorObject> response = baseExceptionHandler.handleMethodArgumentNotValidException(exception);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ErrorEnum.DADOS_INVALIDOS.getErrorCode(), response.getBody().getCodError());
        assertEquals(ErrorEnum.DADOS_INVALIDOS.getErrorMessage(), response.getBody().getMsgError());
    }

    @Test
    void handleExceptionDeveRetornarErroInterno() {
        ResponseEntity<ErrorObject> response = baseExceptionHandler.handleException(new RuntimeException("erro"));

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ErrorEnum.ERRO_INTERNO.getErrorCode(), response.getBody().getCodError());
        assertEquals(ErrorEnum.ERRO_INTERNO.getErrorMessage(), response.getBody().getMsgError());
    }

    private MethodParameter methodParameter() throws NoSuchMethodException {
        Method method = BaseExceptionHandlerTest.class.getDeclaredMethod("metodoComParametro", String.class);

        return new MethodParameter(method, 0);
    }

    @SuppressWarnings("unused")
    private void metodoComParametro(String parametro) {
    }
}
