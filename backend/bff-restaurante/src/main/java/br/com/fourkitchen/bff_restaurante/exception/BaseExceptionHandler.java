package br.com.fourkitchen.bff_restaurante.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BaseExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorObject> handleBaseException(BaseException e) {
        log.warn("Erro de negocio no bff-restaurante: {}", e.getMessage());

        return buildErrorResponse(e.getErrorEnum());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorObject> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        final String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorEnum.DADOS_INVALIDOS.getErrorMessage());

        return buildErrorResponse(ErrorEnum.DADOS_INVALIDOS, errorMessage);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorObject> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return buildErrorResponse(ErrorEnum.DADOS_INVALIDOS, "Período informado é inválido.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorObject> handleException(Exception e) {
        log.error("Erro inesperado no bff-restaurante", e);

        return buildErrorResponse(ErrorEnum.ERRO_INTERNO);
    }

    private ResponseEntity<ErrorObject> buildErrorResponse(ErrorEnum errorEnum) {
        return buildErrorResponse(errorEnum, errorEnum.getErrorMessage());
    }

    private ResponseEntity<ErrorObject> buildErrorResponse(ErrorEnum errorEnum, String errorMessage) {
        ErrorObject errorObject = ErrorObject.builder()
                .codError(errorEnum.getErrorCode())
                .msgError(errorMessage)
                .build();

        return ResponseEntity
                .status(errorEnum.getHttpStatus())
                .body(errorObject);
    }
}
